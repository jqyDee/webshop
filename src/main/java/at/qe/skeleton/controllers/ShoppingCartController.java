package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.ShoppingCartItemDTO;
import at.qe.skeleton.model.Userx;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {
    public ShoppingCartController() {

    }

    @GetMapping("")
    public ResponseEntity<Collection<ShoppingCartItemDTO>> getShoppingCart(
            @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("")
    public ResponseEntity<Collection<ShoppingCartItemDTO>> addAllToShoppingCart(
            @RequestBody Collection<ShoppingCartItemDTO> cartItems,
            @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @DeleteMapping("")
    public ResponseEntity<Void> clearShoppingCart(@AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ShoppingCartItemDTO> addProductToShoppingCart(
            @PathVariable Long productId, @RequestParam(required = true) int quantity,
            @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductFromShoppingCart(@PathVariable Long productId,
                                                              @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ShoppingCartItemDTO> updateProductInShoppingCart(
            @PathVariable Long productId, @RequestParam(required = true) int quantity,
            @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
