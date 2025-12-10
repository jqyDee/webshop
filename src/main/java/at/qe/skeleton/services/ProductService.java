package at.qe.skeleton.services;

import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.repositories.ProductRepository;
import at.qe.skeleton.specifications.ProductSpecification;
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
     * @param sort how the output should be sorted
     * @param filter filterDTO for the products or null
     * @return collection of filtered and paged products
     */
    public Collection<Product> getProducts(Integer pageId, Integer pageSize, Sort sort,
                                           ProductFilterDTO filter) {
        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        Specification<Product> spec = ProductSpecification.createFromFilterDTO(filter);

        return productRepository.findAll(spec, pageable).getContent();
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

    /**
     * Update the rating of a product. This should be called when a review is created or deleted.
     * (In future also review updates)
     *
     * @param product the product to update the rating for
     */
    @Transactional
    public void updateRating(Product product) {
        // TODO: when reviews are done; getAverage call should be DB query
        // Double calculatedAverage = reviewRepository.getAverageRatingByProduct(product);
        Double calculatedAverage = null;

        double newRating = (calculatedAverage != null) ? calculatedAverage : 0.0;

        product.setRating(newRating);

        productRepository.save(product);
    }
}
