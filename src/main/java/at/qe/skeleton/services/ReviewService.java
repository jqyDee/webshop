package at.qe.skeleton.services;

import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Review;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.model.UserxRole;
import at.qe.skeleton.repositories.ProductRepository;
import at.qe.skeleton.repositories.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductService productService,
                         ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.productRepository = productRepository;
    }

    /**
     * Get Reviews for the specified page. If parameters are left null they are ignored.
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
    public Optional<Product> addReview(Long productId, Review newReview, Userx author) throws
                                                                                       AccessDeniedException {
        if (productId == null || newReview == null || author == null) {
            throw new IllegalArgumentException("Product id or review or user is null");
        }

        Product product = productService.loadProduct(productId).orElseThrow(EntityNotFoundException::new);

        newReview.setAuthor(author);
        product.addReview(newReview);

        productService.updateRating(product);

        return Optional.of(productRepository.save(product));
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

        Product product = productService.loadProduct(productId).orElseThrow(EntityNotFoundException::new);

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
                   productService.updateRating(product);
                   productRepository.save(product);
               });

    }

}
