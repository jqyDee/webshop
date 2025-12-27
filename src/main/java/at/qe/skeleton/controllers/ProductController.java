package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.dtos.ReviewDTO;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.mappers.ReviewMapper;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Review;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper,
                             ReviewMapper reviewMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.reviewMapper = reviewMapper;
    }

    /**
     * GET all products / products for a pageable list component. Filtering and Sorting can be
     * applied as well
     *
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort how the output should be sorted
     * @param filter filterDTO for the products or null
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of products on
     *         the specified page with the specified filters and sorting
     */
    @GetMapping("/products")
    public ResponseEntity<PageableListDTO<ProductDTO>> getProducts(
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
                productPage.getContent().stream().map(productMapper::mapTo).toList()
        );

        return ResponseEntity.ok(pageableListDTO);
    }

    /**
     * GET one product
     *
     * @param id the id to search for
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with the product of given id in
     *         the body, or with status {@code 404} if no such product exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productService.loadProduct(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return ResponseEntity.ok(productMapper.mapTo(product));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET reviews for a specific product
     *
     * @param id product id to get the reviews from
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort how the output should be sorted
     * @return {@link ResponseEntity} with status {@code 200 (ok)} with a collection of reviews on
     *         the specific page with the specific sorting
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageableListDTO<ReviewDTO>> getReviews(@PathVariable Long id,
                                                                 @RequestParam(required = false) Integer pageId,
                                                                 @RequestParam(required = false) Integer pageSize,
                                                                 @SortDefault(sort = "name", direction = Sort.Direction.ASC) Sort sort
    ) {
        Page<Review> reviewPage = productService.getReviews(id, pageId, pageSize, sort);

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
     * @param id product id to create the review at
     * @param reviewDto the review DTO of the to be created review
     * @param user the current user
     * @return the updated product
     */
    @PostMapping("/{id}/createReview")
    public ResponseEntity<ProductDTO> createReview(@PathVariable Long id,
                                                  @Valid @RequestBody ReviewDTO reviewDto,
                                                  @AuthenticationPrincipal Userx user) {
        Optional<Product> product;

        product = productService.addReview(id, reviewMapper.mapFrom(reviewDto), user);

        return product.map(value -> ResponseEntity.ok(productMapper.mapTo(value)))
                      .orElseGet(() -> ResponseEntity.notFound().build());

    }

    /**
     * DELETE a review from a product
     *
     * @param productId product id to delete the review at
     * @param reviewId review id which should be deleted
     * @param user current user
     * @return {@code 200 (ok)} or {@code 403 (FORBIDDEN)} when access denied
     */
    @DeleteMapping("/{productId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long productId,
                                             @PathVariable Long reviewId,
                                             @AuthenticationPrincipal Userx user) {
        productService.removeReview(productId, reviewId, user);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<String> subscribe(@PathVariable Long id,
                                            @RequestParam(required = false) boolean inStock,
                                            @RequestParam(required = false) boolean discount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PatchMapping("/{id}/subscribe")
    public ResponseEntity<String> updateSubscription(@PathVariable Long id,
                                                     @RequestParam(required = false) boolean inStock,
                                                     @RequestParam(required = false) boolean discount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
