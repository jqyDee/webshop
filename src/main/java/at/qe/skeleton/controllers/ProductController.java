package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.dtos.ReviewDTO;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.mappers.ReviewMapper;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.ProductSubscriptionRepository;
import at.qe.skeleton.services.ProductService;
import at.qe.skeleton.services.ProductSubscriptionService;
import at.qe.skeleton.services.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Product REST api endpoints exposed by the server
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;
    private final ProductSubscriptionService productSubscriptionService;
    private final ProductSubscriptionRepository productSubscriptionRepository;
    private final ReviewService reviewService;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper,
                             ReviewMapper reviewMapper, ProductSubscriptionService productSubscriptionService, ProductSubscriptionRepository productSubscriptionRepository,
                             ReviewService reviewService) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.reviewMapper = reviewMapper;
        this.productSubscriptionService = productSubscriptionService;
        this.productSubscriptionRepository = productSubscriptionRepository;
        this.reviewService = reviewService;
    }

    /**
     * GET all products / products for a pageable list component. Filtering and Sorting can be
     * applied as well
     *
     * @param pageId   id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort     how the output should be sorted
     * @param filter   filterDTO for the products or null
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of products on
     * the specified page with the specified filters and sorting
     */
    @GetMapping("/products")
    public ResponseEntity<PageableListDTO<ProductDTO>> getProducts(
            @AuthenticationPrincipal Userx user,
            @RequestParam(required = false) Integer pageId,
            @RequestParam(required = false) Integer pageSize,
            @SortDefault(sort = "name", direction = Sort.Direction.ASC) Sort sort,
            @ModelAttribute ProductFilterDTO filter
    ) {

        Page<Product> productPage = productService.getProducts(pageId, pageSize, sort, filter);

        PageableListDTO<ProductDTO> pageableListDTO = new PageableListDTO<>(
                pageSize,
                (pageId != null) ? pageId + 1 : null,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getContent().stream().map(p -> productMapper.mapTo(p, getSubscriptions(user, p))).toList()
        );

        return ResponseEntity.ok(pageableListDTO);
    }

    /**
     * Get product subscriptions of a user and a product. Helper method to add them to the ProductDTO
     *
     * @param user user of the subscriptions
     * @param product product the subscriptions are for
     * @return Map of ProductEventType and boolean (Type, enabled).
     */
    private Map<ProductEventType, Boolean> getSubscriptions(Userx user, Product product) {
        // init map with no subscriptions as a anonymous user would also have no subscriptions
        Map<ProductEventType, Boolean> subscriptions = new EnumMap<>(ProductEventType.class);
        subscriptions.put(ProductEventType.BACK_IN_STOCK, false);
        subscriptions.put(ProductEventType.FOR_SALE, false);
        if (user == null) {
            return subscriptions;
        }
        Optional<ProductSubscription> sub = productSubscriptionRepository
                .findByProductIdAndUser(product.getId(), user);
        sub.ifPresent(
                subscription -> subscription
                        .getNotifyOn()
                        .forEach(notify -> subscriptions.put(notify, true))
        );
        return subscriptions;
    }

    /**
     * GET one product
     *
     * @param id the id to search for
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with the product of given id in
     * the body, or with status {@code 404} if no such product exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@AuthenticationPrincipal Userx user, @PathVariable Long id) {
        Product product = productService.loadProduct(id).orElseThrow(EntityNotFoundException::new);

        Map<ProductEventType, Boolean> subscriptions = getSubscriptions(user, product);
        ProductDTO p = productMapper.mapTo(product, subscriptions);
        return ResponseEntity.ok(p);
    }

    /**
     * GET reviews for a specific product
     *
     * @param id       product id to get the reviews from
     * @param pageId   id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort     how the output should be sorted
     * @return {@link ResponseEntity} with status {@code 200 (ok)} with a collection of reviews on
     * the specific page with the specific sorting
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageableListDTO<ReviewDTO>> getReviews(@PathVariable Long id,
                                                                 @RequestParam(required = false) Integer pageId,
                                                                 @RequestParam(required = false) Integer pageSize,
                                                                 @SortDefault(sort = "createdDate", direction = Sort.Direction.ASC) Sort sort
    ) {
        Page<Review> reviewPage = reviewService.getReviews(id, pageId, pageSize, sort);

        PageableListDTO<ReviewDTO> pageableListDTO = new PageableListDTO<>(
                pageSize,
                (pageId != null) ? pageId + 1 : null,
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements(),
                reviewPage.getContent().stream().map(reviewMapper::mapTo).toList()
        );

        return ResponseEntity.ok(pageableListDTO);
    }

    /**
     * POST create a new review for a product
     * Note: returning product as rating changes as well
     *
     * @param id        product id to create the review at
     * @param reviewDto the review DTO of the to be created review
     * @param user      the current user
     * @return the updated product
     */
    @PostMapping("/{id}/createReview")
    public ResponseEntity<ProductDTO> createReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDto,
            @AuthenticationPrincipal Userx user) {
        Product product = reviewService.addReview(id, reviewMapper.mapFrom(reviewDto), user).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(productMapper.mapTo(product));
    }

    /**
     * DELETE a review from a product
     *
     * @param productId product id to delete the review at
     * @param reviewId  review id which should be deleted
     * @param user      current user
     * @return {@code 200 (ok)} or {@code 403 (FORBIDDEN)} when access denied
     */
    @DeleteMapping("/{productId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long productId,
                                             @PathVariable Long reviewId,
                                             @AuthenticationPrincipal Userx user) {
        reviewService.removeReview(productId, reviewId, user);

        return ResponseEntity.ok().build();
    }

    /**
     * Toggle a subscription of a user and a product.
     *
     * @param user currently authenticated user
     * @param id product id of the product to enable the subscription for
     * @param flip type of the product subscription
     * @return {@code 200 (ok)} or {@code 403 (FORBIDDEN)} when access denied
     */
    @PatchMapping("/{id}/subscribe")
    public ResponseEntity<Void> updateSubscription(
            @AuthenticationPrincipal Userx user,
            @PathVariable Long id,
            @RequestParam ProductEventType flip) {
        Optional<ProductSubscription> exists = productSubscriptionRepository.findByProductIdAndUser(id, user);
        if (exists.isEmpty()) {
            productSubscriptionService.addProductSubscription(user, id, flip);
            return ResponseEntity.ok().build();
        }
        boolean shouldAdd = !exists.get().getNotifyOn().contains(flip);
        if (shouldAdd) {
            productSubscriptionService.addProductSubscription(user, id, flip);
        } else {
            productSubscriptionService.removeProductSubscriptionEvent(user, id, flip);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Unsubscribe all product subscriptions of a specific product.
     *
     * @param user currently authenticated user
     * @param id product id to unsubscribe all subscriptions from
     * @return {@code 200 (ok)} or {@code 403 (FORBIDDEN)} when access denied
     */
    @DeleteMapping("/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@AuthenticationPrincipal Userx user, @PathVariable Long id) {
        productSubscriptionService.deleteProductSubscription(user, id);
        return ResponseEntity.ok().build();
    }
}
