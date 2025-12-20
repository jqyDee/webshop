package at.qe.skeleton.tests;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.CartService;
import at.qe.skeleton.services.ProductService;
import at.qe.skeleton.services.UserxService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Tests for the Cart Service
 */
@SpringBootTest
public class CartServiceTest {
    @Autowired
    CartService cartService;
    @Autowired
    private UserxService userxService;
    @Autowired
    private ProductService productService;

    @Test
    public void testDataInitialization() {
        Userx u1 = userxService.getUserByUsername("user1");
        Userx u2 = userxService.getUserByUsername("user2");
        Userx u3 = userxService.getUserByUsername("elvis");

        List<CartItem> itemsU1 = cartService.getCartItems(u1).stream().toList();
        List<CartItem> itemsU2 = cartService.getCartItems(u2).stream().toList();
        List<CartItem> itemsU3 = cartService.getCartItems(u3).stream().toList();

        Assertions.assertEquals(2, itemsU1.size());
        Assertions.assertEquals(1, itemsU2.size());
        Assertions.assertEquals(1, itemsU3.size());

        Assertions.assertEquals(1, itemsU1.getFirst().getQuantity());
        Assertions.assertEquals(1, itemsU1.get(1).getQuantity());

        Assertions.assertEquals(1, itemsU2.getFirst().getQuantity());

        Assertions.assertEquals(5, itemsU3.getFirst().getQuantity());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testAddItemToCartUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");

        CartItem c1 = new CartItem();
        c1.setQuantity(1);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, c1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testAddItemToCartUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");

        CartItem c1 = new CartItem();
        c1.setQuantity(1);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, c1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testAddItemToCartAuthorizedUser() {
        Userx u1 = userxService.getUserByUsername("user1");
        Optional<Product> p1Opt = productService.loadProduct(1000L);

        Assertions.assertTrue(p1Opt.isPresent());
        Product p1 = p1Opt.get();

        CartItem c1 = new CartItem();
        c1.setQuantity(2);
        c1.setProduct(p1);

        cartService.saveCartItem(u1, c1);

        Optional<CartItem> addedCartItemOpt = cartService.getCartItems(u1).stream()
                                                         .filter(c -> c.getQuantity() == c1.getQuantity())
                                                         .findFirst();
        Assertions.assertTrue(addedCartItemOpt.isPresent());
        CartItem addedCartItem = addedCartItemOpt.get();


        Assertions.assertEquals(3, cartService.getCartItems(u1).size());
        Assertions.assertNotNull(addedCartItem.getProduct());
        Assertions.assertNotNull(addedCartItem.getUser());
        Assertions.assertEquals("user1",  addedCartItem.getUser().getUsername());
        Assertions.assertEquals("Iphone 15", addedCartItem.getProduct().getName());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testUpdateCartItemUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");
        Userx u =  userxService.getUserByUsername("user1");

        Optional<CartItem> cartItemToUpdateOpt = cartService.getCartItems(u).stream().findFirst();

        Assertions.assertTrue(cartItemToUpdateOpt.isPresent());
        CartItem cartItemToBeUpdated = cartItemToUpdateOpt.get();

        Assertions.assertEquals(1, cartItemToBeUpdated.getQuantity());
        cartItemToBeUpdated.setQuantity(2);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, cartItemToBeUpdated));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testUpdateCartItemUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");
        Userx u =  userxService.getUserByUsername("user1");

        Optional<CartItem> cartItemToUpdateOpt = cartService.getCartItems(u).stream().findFirst();

        Assertions.assertTrue(cartItemToUpdateOpt.isPresent());
        CartItem cartItemToBeUpdated = cartItemToUpdateOpt.get();

        Assertions.assertEquals(1, cartItemToBeUpdated.getQuantity());
        cartItemToBeUpdated.setQuantity(2);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.saveCartItem(u1, cartItemToBeUpdated));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testUpdateCartItemAuthorizedUser() {
        Userx u1 = userxService.getUserByUsername("user1");

        Optional<CartItem> cartItemToUpdateOpt = cartService.getCartItems(u1).stream().findFirst();

        Assertions.assertTrue(cartItemToUpdateOpt.isPresent());
        CartItem cartItemToBeUpdated = cartItemToUpdateOpt.get();

        Assertions.assertEquals(1, cartItemToBeUpdated.getQuantity());
        cartItemToBeUpdated.setQuantity(2);

        cartService.saveCartItem(u1, cartItemToBeUpdated);

        Assertions.assertEquals(2, cartService.getCartItems(u1).size());
        Optional<CartItem> cartItemUpdated =  cartService.getCartItems(u1).stream().findFirst();
        Assertions.assertTrue(cartItemUpdated.isPresent());
        Assertions.assertEquals(2, cartItemUpdated.get().getQuantity());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testRemoveCartItemUnauthorizedAdmin() {
        Userx u1 = userxService.getUserByUsername("admin");
        Userx u =  userxService.getUserByUsername("user1");
        Optional<CartItem> cartItemOpt = cartService.getCartItems(u).stream().findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.removeCartItem(u1, cartItem));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testRemoveCartItemUnauthorizedManager() {
        Userx u1 = userxService.getUserByUsername("manager");
        Userx u =  userxService.getUserByUsername("user1");
        Optional<CartItem> cartItemOpt = cartService.getCartItems(u).stream().findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.removeCartItem(u1, cartItem));
    }

    @Test
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testRemoveCartItemUnauthorizedUser() {
        Userx u1 =  userxService.getUserByUsername("user2");
        Userx u = userxService.getUserByUsername("user1");

        Optional<CartItem> cartItemOpt = cartService.getCartItems(u).stream().findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                                () -> cartService.removeCartItem(u1, cartItem));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testRemoveCartItemAuthorizedUser() {
        Userx u =  userxService.getUserByUsername("user1");

        Optional<CartItem> cartItemOpt = cartService.getCartItems(u).stream().findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();

        cartService.removeCartItem(u, cartItem);

        Collection<CartItem> cartItems = cartService.getCartItems(u);
        Assertions.assertEquals(1, cartItems.size());
        Assertions.assertFalse(cartItems.contains(cartItem));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testClearCartItems() {
        Userx u =  userxService.getUserByUsername("user1");

        cartService.clearCartItems(u);

        Assertions.assertTrue(cartService.getCartItems(u).isEmpty());
    }
}
