package at.qe.skeleton.tests;

import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.services.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    ProductService productService;

    @Test
    public void testDataInitialization() {
        Assertions.assertEquals(4, productService.getProducts(null, null, null, null).getContent().size(),
                                "Insufficient amount of products initialized for test data source");

        for (Product product : productService.getProducts(null, null, null, null)) {
            switch (product.getName()) {
                case "Iphone 12":
                case "Iphone 13":
                case "Iphone 14":
                case "Iphone 15":
                    Assertions.assertNotNull(product.getName(),
                                             "Product " +  product.getName() + " does not have name");
                    Assertions.assertNotNull(product.getCreatedDate(),
                                             "Product " +  product.getName() + " does not have create Date");
                    break;
                default: Assertions.fail("Unknown product name " + product.getName());
            }
        }
    }

    @Test
    public void testGetProductsPaged() {
        List<Product> productsPage1Expected = new ArrayList<>();
        List<Product> productsPage2Expected = new ArrayList<>();

        Optional<Product> product0 = productService.loadProduct(1000L);
        Optional<Product> product1 = productService.loadProduct(2000L);
        Optional<Product> product2 = productService.loadProduct(3000L);
        Optional<Product> product3 = productService.loadProduct(4000L);
        Assertions.assertTrue(product0.isPresent());
        Assertions.assertTrue(product1.isPresent());
        Assertions.assertTrue(product2.isPresent());
        Assertions.assertTrue(product3.isPresent());

        productsPage1Expected.add(product0.get());
        productsPage1Expected.add(product1.get());
        productsPage1Expected.add(product2.get());
        productsPage2Expected.add(product3.get());

        Collection<Product> productsPaged1 = productService.getProducts(0, 3, null, null).getContent();
        Collection<Product> productsPaged2 = productService.getProducts(1, 3, null, null).getContent();
        Collection<Product> productsPaged3 = productService.getProducts(2, 3, null, null).getContent();

        Assertions.assertEquals(4, productService.getProducts(0, 3, null, null).getTotalElements(),
                                "Total element count is wrong");

        Assertions.assertEquals(3, productsPaged1.size(), "Insufficient amount of products retrieved");
        Assertions.assertEquals(productsPage1Expected, productsPaged1, "Wrong products in page");

        Assertions.assertEquals(1, productsPaged2.size(), "Insufficient amount of products retrieved");
        Assertions.assertEquals(productsPage2Expected, productsPaged2, "Wrong products in page");

        Assertions.assertEquals(0, productsPaged3.size(), "Too many products retrieved");
    }

    @Test
    public void testGetProductsFiltered() {
        // Single Filter Tests
        ProductFilterDTO spec1 = new ProductFilterDTO("13", null, null, null, null);
        ProductFilterDTO spec2 = new ProductFilterDTO(null, null, 300.0, 400.0, null);
        ProductFilterDTO spec3 = new ProductFilterDTO(null, null, null, 400.0, null);
        ProductFilterDTO spec4 = new ProductFilterDTO(null, null, 300.0, null, null);
        ProductFilterDTO spec5 = new ProductFilterDTO(null, 4.0, null, null, null);
        ProductFilterDTO spec6 = new ProductFilterDTO(null, null, null, null, 9);

        Assertions.assertEquals(1, productService.getProducts(null, null, null, spec1).getContent().size(),
                                "Insufficient amount of products retrieved");

        Assertions.assertEquals(1, productService.getProducts(null, null, null, spec2).getContent().size(),
                                "Insufficient amount of products retrieved");
        Assertions.assertEquals(2, productService.getProducts(null, null, null, spec3).getContent().size(),
                                "Insufficient amount of products retrieved");
        Assertions.assertEquals(3, productService.getProducts(null, null, null, spec4).getContent().size(),
                                "Insufficient amount of products retrieved");
        Assertions.assertEquals(1, productService.getProducts(null, null, null, spec5).getContent().size(),
                                "Insufficient amount of products retrieved");
        Assertions.assertEquals(1, productService.getProducts(null, null, null, spec6).getContent().size(),
                                "Insufficient amount of products retrieved");

        // Combination
        ProductFilterDTO spec7 = new ProductFilterDTO("13", 4.0, 300.0, 400.0, 9);
        Assertions.assertEquals(1, productService.getProducts(null, null, null, spec7).getContent().size(),
                                "Insufficient amount of products retrieved");
    }

    @Test
    public void testGetProductsSorted() {
        Optional<Product> productOpt0 = productService.loadProduct(1000L);
        Optional<Product> productOpt1 = productService.loadProduct(2000L);
        Optional<Product> productOpt2 = productService.loadProduct(3000L);
        Optional<Product> productOpt3 = productService.loadProduct(4000L);
        Assertions.assertTrue(productOpt0.isPresent());
        Assertions.assertTrue(productOpt1.isPresent());
        Assertions.assertTrue(productOpt2.isPresent());
        Assertions.assertTrue(productOpt3.isPresent());
        Product product0 = productOpt0.get();
        Product product1 = productOpt1.get();
        Product product2 = productOpt2.get();
        Product product3 = productOpt3.get();

        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        Collection<Product> products = productService.getProducts(0, 4, sort, null).getContent();

        Assertions.assertEquals(product1, products.stream().toList().get(0));
        Assertions.assertEquals(product3, products.stream().toList().get(1));
        Assertions.assertEquals(product2, products.stream().toList().get(2));
        Assertions.assertEquals(product0, products.stream().toList().get(3));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testSaveProductManager() {
        String name = "Iphone 11";
        double price = 100.0;
        int stock = 10;
        double discount = 0.2;
        String shortDescription = "Iphone 11 short description";
        String description = "Iphone 11 full description";
        String imageUrl = "https://www.google.com";

        Product toBeCreatedProduct = new Product();
        toBeCreatedProduct.setName(name);
        toBeCreatedProduct.setPrice(price);
        toBeCreatedProduct.setStock(stock);
        toBeCreatedProduct.setDiscount(discount);
        toBeCreatedProduct.setShortDescription(shortDescription);
        toBeCreatedProduct.setDescription(description);
        toBeCreatedProduct.setImageUrl(imageUrl);

        Product savedProduct = productService.saveProduct(toBeCreatedProduct);

        Optional<Product> freshlySavedProductOpt = productService.loadProduct(savedProduct.getId());
        Assertions.assertFalse(freshlySavedProductOpt.isEmpty(),
                               "New Product could not be loaded from test data source after being saved");
        Product freshlySavedProduct =  freshlySavedProductOpt.get();

        Assertions.assertNotNull(freshlySavedProduct.getId(),
                                 "Id of saved product was not initialized");

        Assertions.assertEquals(name, freshlySavedProduct.getName(),
                                "Name of saved product is not matching");
        Assertions.assertEquals(price, freshlySavedProduct.getPrice(),
                                "Price of saved product is not matching");
        Assertions.assertEquals(stock, freshlySavedProduct.getStock(),
                                "Stock of saved product is not matching");
        Assertions.assertEquals(discount, freshlySavedProduct.getDiscount(),
                                "Discount of saved product is not matching");
        Assertions.assertEquals(shortDescription, freshlySavedProduct.getShortDescription(),
                                "Short Description of saved product is not matching");
        Assertions.assertEquals(description, freshlySavedProduct.getDescription(),
                                "description of saved product is not matching");
        Assertions.assertEquals(imageUrl, freshlySavedProduct.getImageUrl(),
                                "Image URL of saved product is not matching");

        Assertions.assertNotNull(freshlySavedProduct.getCreatedDate(),
                                 "Created Date of saved product was not initialized");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testSaveProductAdmin() {
        String name = "Iphone 11";
        double price = 100.0;
        int stock = 10;
        double discount = 0.2;
        String shortDescription = "Iphone 11 short description";
        String description = "Iphone 11 full description";
        String imageUrl = "https://www.google.com";

        Product toBeCreatedProduct = new Product();
        toBeCreatedProduct.setName(name);
        toBeCreatedProduct.setPrice(price);
        toBeCreatedProduct.setStock(stock);
        toBeCreatedProduct.setDiscount(discount);
        toBeCreatedProduct.setShortDescription(shortDescription);
        toBeCreatedProduct.setDescription(description);
        toBeCreatedProduct.setImageUrl(imageUrl);

        Product savedProduct = productService.saveProduct(toBeCreatedProduct);

        Optional<Product> freshlySavedProductOpt = productService.loadProduct(savedProduct.getId());
        Assertions.assertFalse(freshlySavedProductOpt.isEmpty(),
                               "New Product could not be loaded from test data source after being saved");
        Product freshlySavedProduct =  freshlySavedProductOpt.get();

        Assertions.assertNotNull(freshlySavedProduct.getId(),
                                 "Id of saved product was not initialized");

        Assertions.assertEquals(name, freshlySavedProduct.getName(),
                                "Name of saved product is not matching");
        Assertions.assertEquals(price, freshlySavedProduct.getPrice(),
                                "Price of saved product is not matching");
        Assertions.assertEquals(stock, freshlySavedProduct.getStock(),
                                "Stock of saved product is not matching");
        Assertions.assertEquals(discount, freshlySavedProduct.getDiscount(),
                                "Discount of saved product is not matching");
        Assertions.assertEquals(shortDescription, freshlySavedProduct.getShortDescription(),
                                "Short Description of saved product is not matching");
        Assertions.assertEquals(description, freshlySavedProduct.getDescription(),
                                "description of saved product is not matching");
        Assertions.assertEquals(imageUrl, freshlySavedProduct.getImageUrl(),
                                "Image URL of saved product is not matching");

        Assertions.assertNotNull(freshlySavedProduct.getCreatedDate(),
                                 "Created Date of saved product was not initialized");

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testDeleteProductByAdmin() {
        Long deleteProductId = 1000L;
        Optional<Product> productOpt = productService.loadProduct(deleteProductId);
        Assertions.assertFalse(productOpt.isEmpty(),
                               "Product could not be loaded");

        Product toBeDeletedProduct = productOpt.get();
        productService.deleteProduct(toBeDeletedProduct);

        Assertions.assertEquals(3, productService.getProducts(null, null, null, null).getContent().size(),
                                "No Product has been deleted after calling deleteProduct");
        Optional<Product> freshlyDeletedProduct = productService.loadProduct(deleteProductId);
        Assertions.assertTrue(freshlyDeletedProduct.isEmpty(),
                              "Product should be deleted but could be loaded");

        for (Product product : productService.getProducts(null, null, null, null)) {
            Assertions.assertNotEquals(product.getName(), toBeDeletedProduct.getName(),
                                       "Deleted Product could still be loaded from the test data");
        }
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testDeleteProductByManager() {
        Long deleteProductId = 1000L;
        Optional<Product> productOpt = productService.loadProduct(deleteProductId);
        Assertions.assertFalse(productOpt.isEmpty(),
                               "Product could not be loaded");

        Product toBeDeletedProduct = productOpt.get();
        productService.deleteProduct(toBeDeletedProduct);

        Assertions.assertEquals(3, productService.getProducts(null, null, null, null).getContent().size(),
                                "No Product has been deleted after calling deleteProduct");
        Optional<Product> freshlyDeletedProduct = productService.loadProduct(deleteProductId);
        Assertions.assertTrue(freshlyDeletedProduct.isEmpty(),
                              "Product should be deleted but could be loaded");

        for (Product product : productService.getProducts(null, null, null, null)) {
            Assertions.assertNotEquals(product.getName(), toBeDeletedProduct.getName(),
                                       "Deleted Product could still be loaded from the test data");
        }
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "normalUser", authorities = {"CUSTOMER"})
    public void testUnauthorizedSaveProduct() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            Long productId = 1000L;
            Optional<Product> productOpt = productService.loadProduct(productId);
            Assertions.assertFalse(productOpt.isEmpty());
            Product product = productOpt.get();

            Assertions.assertEquals(productId, product.getId(),
                                    "Call to productService returned wrong product");

            productService.saveProduct(product);
        });
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "normalUser", authorities = {"CUSTOMER"})
    public void testUnauthorizedDeleteProduct() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            Long productId = 1000L;
            Optional<Product> productOpt = productService.loadProduct(productId);
            Assertions.assertFalse(productOpt.isEmpty());
            Product product = productOpt.get();

            Assertions.assertEquals(productId, product.getId(),
                                    "Call to productService returned wrong product");

            productService.deleteProduct(product);
        });
    }

    @Test
    public void testCheckStock() {
        Long productId = 1000L;
        int quantity = 2;

        Assertions.assertFalse(productService.checkStock(productId, quantity));

        quantity = 1;
        Assertions.assertTrue(productService.checkStock(productId, quantity));
    }

    @DirtiesContext
    @Test
    public void testReserveStock() {
        Long productId = 1000L;
        int quantity = 2;

        Optional<Product> productBeforeOpt = productService.loadProduct(productId);
        Assertions.assertFalse(productBeforeOpt.isEmpty());
        Product productBefore = productBeforeOpt.get();
        Assertions.assertEquals(1, productBefore.getStock());

        Assertions.assertFalse(productService.reserveStock(productId, quantity));
        Optional<Product> productAfterOpt = productService.loadProduct(productId);
        Assertions.assertFalse(productAfterOpt.isEmpty());
        Product productAfter = productAfterOpt.get();
        Assertions.assertEquals(1, productAfter.getStock());

        quantity = 1;
        Assertions.assertTrue(productService.reserveStock(productId, quantity));
        productAfterOpt = productService.loadProduct(productId);
        Assertions.assertFalse(productAfterOpt.isEmpty());
        productAfter = productAfterOpt.get();
        Assertions.assertEquals(0, productAfter.getStock());
    }
}
