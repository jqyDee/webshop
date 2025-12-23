package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.CartItemDTO;
import at.qe.skeleton.mappers.CartItemMapper;
import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final CartItemMapper cartItemMapper;

    public CartController(CartService cartService, CartItemMapper cartItemMapper) {
        this.cartService = cartService;
        this.cartItemMapper = cartItemMapper;
    }

    /**
     * GET all cart items of the currently authenticated user
     *
     * @param user automatically through security context
     * @return collection of {@link CartItemDTO}
     */
    @GetMapping("")
    public ResponseEntity<Collection<CartItemDTO>> getShoppingCart(
            @AuthenticationPrincipal Userx user) {
        Collection<CartItem> cartItems = cartService.getCartItems(user);
        Collection<CartItemDTO> cartItemDTOs = cartItems.stream().map(cartItemMapper::mapTo).toList();

        return ResponseEntity.ok(cartItemDTOs);
    }

    /**
     * ADD map of {productId, quantity} to the database
     *
     * @param cartItems Map of productId and quantity
     * @param user currently authenticated user
     * @return VOID
     */
    @PostMapping("")
    public ResponseEntity<Void> addAllToShoppingCart(
            @RequestBody Map<Long, Integer> cartItems, // productId, quantity
            @AuthenticationPrincipal Userx user) {
        cartService.saveCartItems(user, cartItems);
        return ResponseEntity.ok().build();
    }

    /**
     * CLEARs the shopping cart of the currently authenticated user
     *
     * @param user currently authenticated user
     * @return VOID
     */
    @DeleteMapping("")
    public ResponseEntity<Void> clearShoppingCart(@AuthenticationPrincipal Userx user) {
        cartService.clearCartItems(user);
        return ResponseEntity.ok().build();
    }

    /**
     * ADD cart item {productId, quantity} to the database
     *
     * @param productId id of product the cart item will be created of
     * @param quantity quantity of the product in the cart item
     * @param user currently authenticated user
     * @return VOID
     */
    @PostMapping("/{productId}")
    public ResponseEntity<Void> addProductToShoppingCart(
            @PathVariable Long productId, @RequestParam int quantity,
            @AuthenticationPrincipal Userx user) {
        cartService.saveCartItem(user, productId, quantity);

        return ResponseEntity.ok().build();
    }

    /**
     * REMOVE cart item from the database
     *
     * @param productId id of the product the cart item should be deleted from
     * @param user currently authenticated user
     * @return VOID
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductFromShoppingCart(@PathVariable Long productId,
                                                              @AuthenticationPrincipal Userx user) {
        cartService.removeCartItem(user, productId);

        return ResponseEntity.ok().build();
    }

    /**
     * UPDATE the quantity of an existing cart item from the database
     *
     * @param productId id of the product the cart item should be updated from
     * @param quantity of the products
     * @param user currently authenticated user
     * @return VOID
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<CartItemDTO> updateProductInShoppingCart(
            @PathVariable Long productId, @RequestParam int quantity,
            @AuthenticationPrincipal Userx user) {
        cartService.saveCartItem(user, productId, quantity);

        return ResponseEntity.ok().build();
    }
}
