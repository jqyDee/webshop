package at.qe.skeleton.services;

import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.events.ProductEvent;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.ProductRepository;
import at.qe.skeleton.specifications.ProductSpecification;
import at.qe.skeleton.repositories.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service for accessing and manipulating product data.
 */
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ReviewRepository reviewRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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
    public Page<Product> getProducts(Integer pageId, Integer pageSize, Sort sort,
                                           ProductFilterDTO filter) {
        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        Specification<Product> spec = ProductSpecification.createFromFilterDTO(filter);

        return productRepository.findAll(spec, pageable);
    }

    /**
     * Search for product with id in database.
     *
     * @param id id to search in the database
     * @return product matching the id
     */
    public Optional<Product> loadProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }

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
        if (product == null) {
            throw new IllegalArgumentException("Product is null");
        }

        if (product.isNew()) {
            return this.productRepository.save(product);
        }

        Product oldProduct = loadProduct(product.getId()).orElseThrow(EntityNotFoundException::new);
        Product savedProduct =  this.productRepository.save(product);

        publishProductEvents(oldProduct, savedProduct);

        return savedProduct;
    }

    /**
     * Deletes the product from the database.
     *
     * @param product the product to be deleted
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public void deleteProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is null");
        }

        this.productRepository.delete(product);
    }

    /**
     * Check if the specified quantity of a product could potentially be reserved
     *
     * @param productId the productId of the product to check the stock against the needed quantity
     * @param quantity the quantity that wants to be reserved
     * @return true when the quantity could be reserved, false if not.
     */
    @Transactional
    public boolean checkStock(Long productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Id is null");
        }

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
    public boolean reserveStock(Long productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Id is null");
        }

        int rowsUpdated = this.productRepository.reserveStock(productId, quantity);

        return rowsUpdated > 0;
    }

    /** Release stock of order item
     *
     * @param orderItem the order item which stocks should be released
     */
    @Transactional
    public void releaseStock(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem is null");
        }

        this.productRepository.releaseStock(orderItem.getProduct().getId(), orderItem.getQuantity());
        Product product = orderItem.getProduct();
        Product updatedProduct = loadProduct(product.getId()).orElseThrow(EntityNotFoundException::new);
        publishProductEvents(product, updatedProduct);
    }

    /**
     * Get Products for the specified page. If parameters are left null they are ignored.
     *
     * @param productId product id to get the reviews for
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort how the output should be sorted
     * @return page of reviews
     */
    public Page<Review> getReviews(Long productId, Integer pageId, Integer pageSize,
                                   Sort sort) {
        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        // It seems easier to just load the products directly from the product itself,
        // but then pagination is really difficult
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * Add a review to a product
     *
     * @param productId the product id of the product which the review should be saved at
     * @param newReview the new review, IMPORTANT: review_id has to be null
     * @param author the author of the review: get through @AuthorisationPrincipal
     * @return the updated Product
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Optional<Product> addReview(Long productId, Review newReview, Userx author) throws AccessDeniedException {
        if (productId == null || newReview == null || author == null) {
            throw new IllegalArgumentException("Product id or review or user is null");
        }

        Product product = this.loadProduct(productId).orElseThrow(EntityNotFoundException::new);

        newReview.setAuthor(author);
        product.addReview(newReview);

        updateRating(product);

        return Optional.of(this.productRepository.save(product));
    }

    /**
     * Remove a review from a product
     *
     * @param productId the product id of the product which the review should be deleted at
     * @param reviewId the review id of the review tha should be deleted
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void removeReview(Long productId, Long reviewId, Userx currentUser)
            throws AccessDeniedException {
        if (productId == null || reviewId == null || currentUser == null) {
            throw new IllegalArgumentException("Product id or review id is null");
        }

        Product product = this.loadProduct(productId).orElseThrow(EntityNotFoundException::new);

        Collection<Review> reviews = product.getReviews();

        reviews.stream()
               .filter(review -> Objects.equals(review.getId(), reviewId))
               .findFirst()
               .ifPresent(review -> {
                   boolean isAuthor = review.getAuthor() != null && review.getAuthor().equals(currentUser);
                   boolean isAdmin = currentUser.getRole().equals(UserxRole.ADMIN);

                   if (!isAdmin && !isAuthor) {
                       throw new AccessDeniedException(
                               "You are not authorized to perform this action.");
                   }

                   product.removeReview(review);
                   updateRating(product);
                   productRepository.save(product);
               });

    }

    /**
     * Update the rating of a product. This should be called when a review is created or deleted.
     * (In future also review updates)
     *
     * @param product the product to update the rating for
     */
    public void updateRating(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is null");
        }

        Set<Review> reviews = product.getReviews();

        if (reviews == null || reviews.isEmpty()) {
            product.setRating(null);
            return;
        }

        Double calculatedAverage = reviews.stream()
                                          .mapToInt(Review::getRating)
                                          .average()
                                          .orElse(0.0);

        product.setRating(calculatedAverage);
    }

    private void publishProductEvents(Product oldProduct, Product updatedProduct) {
        boolean isBackInStock = oldProduct.getStock() == 0 && updatedProduct.getStock() > 0;
        boolean isDiscounted = oldProduct.getDiscount() < updatedProduct.getDiscount();

        if (isBackInStock) {
            applicationEventPublisher.publishEvent(new ProductEvent(updatedProduct.getId(), ProductEventType.BACK_IN_STOCK));
        }
        if (isDiscounted) {
            applicationEventPublisher.publishEvent(new ProductEvent(updatedProduct.getId(), ProductEventType.FOR_SALE));
        }
    }
}
