package at.qe.skeleton.services;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.model.UserxRole;
import at.qe.skeleton.repositories.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }


    /**
     * Get all cart items for a user
     *
     * @param currentUser user to get the cart items for
     * @return collection of {@link CartItem}
     */
    public Collection<CartItem> getCartItems(Userx currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        return cartItemRepository.findAllByUser(currentUser);
    }

    /**
     * Save a Cart Item to the database
     *
     * @param currentUser the currently authenticated user
     * @param cartItem that should be saved (either new or to be updated)
     * @throws AccessDeniedException when user is not allowed to save an item (non customer, not his own)
     */
    public void saveCartItem(Userx currentUser, CartItem cartItem) throws AccessDeniedException {
        if (currentUser == null
                || cartItem == null) {
            return;
        }

        // 1. check if user is customer
        if (!currentUser.getRoles().contains(UserxRole.CUSTOMER)) {
            throw new AccessDeniedException("You are not authorized to perform this action.");
        }

        // 2. check if cart item is not new
        if (!cartItem.isNew() && !cartItem.getUser().equals(currentUser)) {
            throw new AccessDeniedException("You are not authorized to perform this action.");
        }

        if (cartItem.isNew()) {
            cartItem.setUser(currentUser);
        }

        cartItemRepository.save(cartItem);
    }

    /**
     * Delete a Cart Item from the database
     *
     * @param currentUser the currently authenticated user
     * @param cartItem that should be deleted (if new skipped)
     * @throws AccessDeniedException when user is not allowed to delete an item (non customer, not his own)
     */
    public void removeCartItem(Userx currentUser, CartItem cartItem) throws AccessDeniedException {
        if (currentUser == null
                || cartItem == null
                || cartItem.getUser() == null) {
            return;
        }

        boolean isCustomer = currentUser.getRoles().contains(UserxRole.CUSTOMER);
        boolean isOwner = cartItem.getUser().equals(currentUser);

        if (!isCustomer || !isOwner) {
            throw new AccessDeniedException("You are not authorized to perform this action.");
        }

        if (!cartItem.isNew()) {
            cartItemRepository.delete(cartItem);
        }
    }

    /**
     * Clears the shopping cart of a user
     *
     * @param currentUser the currently authenticated user
     */
    @Transactional
    public void clearCartItems(Userx currentUser) {
        if (currentUser != null) {
            cartItemRepository.deleteAllByUser(currentUser);
        }
    }
}
