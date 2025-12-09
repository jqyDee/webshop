package at.qe.skeleton.services;

import at.qe.skeleton.model.Product;
import at.qe.skeleton.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Service for accessing and manipulating product data.
 */
@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(
            ProductRepository productRepository
    ) {
        this.productRepository = productRepository;
    }

    /**
     * Get Products for the specified page and filter. If parameters are left null they are ignored
     * and all products / filtered products / paged products get returned.
     *
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param filter filter for the products or null
     * @return collection of filtered and paged products
     */
    public Collection<Product> getProducts(Integer pageId, Integer pageSize, Specification<Product> filter, Sort sort) {
        if (pageId != null && pageSize != null && pageSize > 0) {
            Pageable pageable;
            if (sort != null) {
                pageable = PageRequest.of(pageId, pageSize, sort);
            } else {
                pageable = PageRequest.of(pageId, pageSize);
            }

            if (filter != null) {
                return (productRepository.findAll(filter, pageable)).getContent();
            }

            return (productRepository.findAll(pageable)).getContent();
        }

        if (filter != null) {
            return productRepository.findAll(filter);
        }

        return productRepository.findAll();
    }

    /**
     * Search for product with id in database.
     *
     * @param id id to search in the database
     * @return product matching the id
     */
    public Optional<Product> loadProduct(Long id) {
        return this.productRepository.findById(id);
    }

    /**
     * Saves the product in the database. Can also be used to update a product.
     *
     * @param product product to be saved / updated
     * @return the saved product
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public Product saveProduct(Product product) {
        if (product.isNew()) {
            return this.productRepository.save(product);
        }

        // TODO: Discount handling / back in stock handling (ProductSubscriptions)
        return this.productRepository.save(product);
    }

    /**
     * Deletes the product from the database.
     *
     * @param product the product to be deleted
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public void deleteProduct(Product product) {
        this.productRepository.delete(product);

        // TODO: Reviews should be deleted without having to do anything.

        // TODO: ProductSubscriptions that match the product have to be deleted if the product gets
        //       deleted.
    }

    /**
     * Check if the specified quantity of a product could potentially be reserved
     *
     * @param productId the productId of the product to check the stock against the needed quantity
     * @param quantity the quantity that wants to be reserved
     * @return true when the quantity could be reserved, false if not.
     */
    @Transactional
    public boolean checkAvailability(Long productId, int quantity) {
        int rowsMatching = this.productRepository.checkStock(productId, quantity);

        return rowsMatching == 1;
    }

    /**
     * Check if the specified quantity of a product and reserve the quantity by subtracting the
     * quantity from the stock.
     *
     * @param productId the productId of the product to check the stock against the needed quantity
     * @param quantity the quantity that wants to be reserved
     * @return true when the quantity got reserved, false if not.
     */
    @Transactional
    public boolean checkAvailabilityAndReserve(Long productId, int quantity) {
        int rowsUpdated = this.productRepository.decreaseStock(productId, quantity);

        return rowsUpdated > 0;
    }
}
