package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.dtos.ReviewDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    public ProductController() {

    }

    @GetMapping("/products")
    public ResponseEntity<PageableListDTO<ProductDTO>> getProducts(@RequestParam(required = false) int pageId,
                                                                @RequestParam(required = false) int pageSize,
                                                                @RequestParam(required = false) Object filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageableListDTO<ReviewDTO>> getReviews(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("/{id}/createReview")
    public ResponseEntity<ReviewDTO> createReview(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
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
