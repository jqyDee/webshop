package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ManagerController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    /**
     * Creates a product
     *
     * @param productDto the user tb created
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the newly created product in the body
     */
    @PostMapping("/createProduct")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDto) {
        Product product = productService.saveProduct(productMapper.mapFrom(productDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.mapTo(product));
    }

    @PatchMapping("/product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productUpdateDTO) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Deletes product of given id.
     *
     * @param id the id of the product tb deleted
     * @return {@link ResponseEntity} with status {@code 204 (No Content)} on successful delete, or with status {@code 404 (Not Found)} if no product with this id exists
     */

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product existingProduct = productService.loadProduct(id).orElseThrow(EntityNotFoundException::new);

        productService.deleteProduct(existingProduct);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<PageableListDTO<OrderDTO>> getAllOrders() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<Void> updateOrder(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
