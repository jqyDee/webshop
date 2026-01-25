package at.qe.skeleton.tests.services;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.CartItemRepository;
import at.qe.skeleton.services.CartService;
import at.qe.skeleton.services.UserxService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.util.*;

/**
 * Tests for the Cart Service
 */
@SpringBootTest
class CartServiceTest {
    @Autowired
    CartService cartService;
    @Autowired
    private UserxService userxService;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testDataInitialization() {
        Userx u1 = userxService.getUserByUsername("user1");
        Userx u2 = userxService.getUserByUsername("user2");

        List<CartItem> itemsU1 = cartService.getCartItems(u1).stream().toList();
        List<CartItem> itemsU2 = cartService.getCartItems(u2).stream().toList();

        Assertions.assertEquals(3, itemsU1.size());
        Assertions.assertEquals(1, itemsU2.size());

        Assertions.assertEquals(1, itemsU1.getFirst().getQuantity());
        Assertions.assertEquals(1, itemsU1.get(1).getQuantity());

        Assertions.assertEquals(1, itemsU2.getFirst().getQuantity());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testAddItemToCartUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, productId, 1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testAddItemToCartUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, productId, 1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testAddItemToCartAuthorizedUser() {
        Userx u1 = userxService.getUserByUsername("user1");

        Long productId = 3000L;
        int quantity = 4;

        cartService.saveCartItem(u1, productId, quantity);

        Optional<CartItem> addedCartItemOpt = cartService.getCartItems(u1).stream()
                                                         .filter(c -> c.getQuantity() == quantity)
                                                         .findFirst();
        Assertions.assertTrue(addedCartItemOpt.isPresent());
        CartItem addedCartItem = addedCartItemOpt.get();


        Assertions.assertEquals(4, cartService.getCartItems(u1).size());
        Assertions.assertNotNull(addedCartItem.getProduct());
        Assertions.assertNotNull(addedCartItem.getUser());
        Assertions.assertEquals("user1",  addedCartItem.getUser().getUsername());
        Assertions.assertEquals("Iphone 14", addedCartItem.getProduct().getName());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testUpdateCartItemUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, productId, 1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testUpdateCartItemUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, productId, 1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testUpdateCartItemAuthorizedUser() {
        Userx u1 = userxService.getUserByUsername("user1");

        Long productId = 1000L;
        int quantity = 2;

        cartService.saveCartItem(u1, productId, quantity);

        Assertions.assertEquals(3, cartService.getCartItems(u1).size());
        Optional<CartItem> cartItemUpdated =  cartService.getCartItems(u1).stream().findFirst();
        Assertions.assertTrue(cartItemUpdated.isPresent());
        Assertions.assertEquals(quantity, cartItemUpdated.get().getQuantity());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testUpdateCartItemQuantityZero() {
        Userx u1 = userxService.getUserByUsername("user1");

        Long productId = 1000L;
        int quantity = 0;

        Optional<CartItem> cOpt = cartItemRepository.findFirstByUserAndProduct_Id(u1, productId);
        Assertions.assertTrue(cOpt.isPresent());
        CartItem cartItem = cOpt.get();

        cartService.saveCartItem(u1, productId, quantity);
        Assertions.assertEquals(2, cartService.getCartItems(u1).size());

        Assertions.assertFalse(cartService.getCartItems(u1).contains(cartItem));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testRemoveCartItemUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.removeCartItem(u1, productId));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testRemoveCartItemUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");

        Long productId = 1000L;

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.removeCartItem(u1, productId));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testRemoveCartItemAuthorizedUser() {
        Userx u =  userxService.getUserByUsername("user1");

        Long productId = 1000L;

        cartService.removeCartItem(u, productId);

        Collection<CartItem> cartItems = cartService.getCartItems(u);
        Assertions.assertEquals(2, cartItems.size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testClearCartItems() {
        Userx u =  userxService.getUserByUsername("user1");

        cartService.clearCartItems(u);

        Assertions.assertTrue(cartService.getCartItems(u).isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testAddItemsUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");

        Map<Long, Integer> productId_Quantity = new HashMap<>();
        productId_Quantity.put(1000L, 1);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItems(u1, productId_Quantity));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    public void testAddItemsUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("admin");

        Map<Long, Integer> productId_Quantity = new HashMap<>();
        productId_Quantity.put(1000L, 1);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItems(u1, productId_Quantity));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testAddItemsAuthorizedUser() {
        Userx u =  userxService.getUserByUsername("user1");

        Map<Long, Integer> productId_Quantity = new HashMap<>();
        productId_Quantity.put(2000L, 1);
        productId_Quantity.put(3000L, 3);

        cartService.saveCartItems(u, productId_Quantity);

        Collection<CartItem> cartItems = cartService.getCartItems(u);
        Assertions.assertEquals(5, cartItems.size());
        Optional<CartItem> c2Opt = cartItemRepository.findFirstByUserAndProduct_Id(u, 2000L);
        Optional<CartItem> c3Opt = cartItemRepository.findFirstByUserAndProduct_Id(u, 3000L);
        Assertions.assertTrue(c2Opt.isPresent());
        Assertions.assertTrue(c3Opt.isPresent());

        CartItem c2 = c2Opt.get();
        CartItem c3 = c3Opt.get();

        Assertions.assertEquals(1, c2.getQuantity());
        Assertions.assertEquals(3, c3.getQuantity());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testGetCartItemsUserIsNullShouldThrowException() {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> cartService.getCartItems(null));

        Assertions.assertEquals("Current user is null", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testSaveCartItemUserIsNullShouldThrowException() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.saveCartItem(null, 1000L, 1));

        Assertions.assertEquals("Current user or product id is null", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testSaveCartItemProductNotFound() {
        Userx u1 = userxService.getUserByUsername("user1");
        Long nonExistentProductId = 999999L;

        EntityNotFoundException e = Assertions.assertThrows(EntityNotFoundException.class, () -> cartService.saveCartItem(u1, nonExistentProductId, 1));

        Assertions.assertEquals("Product not found", e.getMessage());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testSaveCartItemsUserIsNullShouldThrowException() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.saveCartItems(null, null));

        Assertions.assertEquals("Current user or product/quantity map is null or empty", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testRemoveCartItemUserIsNullShouldThrowException() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.removeCartItem(null, 1000L));

        Assertions.assertEquals("Current user or product id is null", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testClearCartItemUserIsNullShouldThrowException() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.clearCartItems(null));

        Assertions.assertEquals("Current user is null", exception.getMessage());
    }
}