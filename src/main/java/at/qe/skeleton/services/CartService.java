package at.qe.skeleton.services;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.CartItemRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }


    /**
     * Get all cart items for a user
     *
     * @param currentUser user to get the cart items for
     * @return collection of {@link CartItem}
     */
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public Collection<CartItem> getCartItems(Userx currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("Current user is null");
        }

        return cartItemRepository.findAllByUser(currentUser);
    }

    /**
     * Save a cart item to the database
     *
     * @param currentUser current authenticated user
     * @param productId product id of cart item
     * @param quantity quantity of the cart item
     */
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void saveCartItem(Userx currentUser, Long productId, int quantity) {
        if (currentUser == null || productId == null) {
            throw new IllegalArgumentException("Current user or product id is null");
        }

        Optional<CartItem> existingItem = cartItemRepository.findFirstByUserAndProduct_Id(currentUser, productId);

        if (quantity <= 0) {
            removeCartItem(currentUser, productId);
            return;
        }

        if (existingItem.isPresent()) {
            CartItem updatedItem = existingItem.get();
            updatedItem.setQuantity(quantity);
            cartItemRepository.save(updatedItem);
            return;
        }

        Optional<Product> product = productService.loadProduct(productId);
        if (product.isEmpty()) {
            throw new EntityNotFoundException("Product not found");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setUser(currentUser);
        newCartItem.setProduct(product.get());
        newCartItem.setQuantity(quantity);

        cartItemRepository.save(newCartItem);
    }

    /**
     * Save all cart items constructed by the product id, user and quantity given in a map
     *
     * @param currentUser currently authenticated user
     * @param productIdAndQuantity map of product ids and the respective quantity
     */
    @Transactional
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void saveCartItems(Userx currentUser, Map<Long, Integer> productIdAndQuantity) {
        if (currentUser == null ||  productIdAndQuantity == null || productIdAndQuantity.isEmpty()) {
            throw new IllegalArgumentException("Current user or product/quantity map is null or empty");
        }

        for (Map.Entry<Long, Integer> entry : productIdAndQuantity.entrySet()) {
            saveCartItem(currentUser, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Delete a cart item of a user and a product
     *
     * @param currentUser currently authenticated user
     * @param productId id of the product which cart item to delete
     */
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void removeCartItem(Userx currentUser, Long productId) {
        if (currentUser == null || productId == null) {
            throw new IllegalArgumentException("Current user or product id is null");
        }

        Optional<CartItem> existingItem = cartItemRepository.findFirstByUserAndProduct_Id(currentUser, productId);

        existingItem.ifPresent(cartItemRepository::delete);
    }

    /**
     * Clears the shopping cart of a user
     *
     * @param currentUser the currently authenticated user
     */
    @Transactional
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void clearCartItems(Userx currentUser) {
        if (currentUser != null) {
            cartItemRepository.deleteAllByUser(currentUser);
        } else {
            throw new IllegalArgumentException("Current user is null");
        }
    }
}
