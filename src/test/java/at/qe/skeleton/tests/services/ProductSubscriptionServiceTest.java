package at.qe.skeleton.tests.services;

import at.qe.skeleton.model.*;
import at.qe.skeleton.notifications.EmailNotifier;
import at.qe.skeleton.notifications.SMSNotifier;
import at.qe.skeleton.repositories.ProductSubscriptionRepository;
import at.qe.skeleton.services.ProductService;
import at.qe.skeleton.services.ProductSubscriptionService;
import at.qe.skeleton.services.UserxService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductSubscriptionServiceTest {
    @Autowired
    private ProductSubscriptionService productSubscriptionService;
    @Autowired
    private UserxService userxService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductSubscriptionRepository productSubscriptionRepository;
    @MockitoBean
    private EmailNotifier emailNotifier;
    @MockitoBean
    private SMSNotifier smsNotifier;

    @Test
    @DirtiesContext
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testAddSubscriptionEvent() {
        Userx jonny = userxService.getUserByUsername("jonny");
        Product product = productService.loadProduct(3000L).orElseThrow();
        productSubscriptionService.addProductSubscription(jonny, product.getId(), ProductEventType.BACK_IN_STOCK);
        ProductSubscription s = productSubscriptionRepository.findByProductIdAndUser(product.getId(), jonny).orElseThrow();
        Assertions.assertTrue(s.getNotifyOn().contains(ProductEventType.BACK_IN_STOCK));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testAddSubscriptionEvent_EventExists() {
        Userx user2 = userxService.getUserByUsername("customer");
        Product product = productService.loadProduct(1000L).orElseThrow();
        productSubscriptionService.addProductSubscription(user2, product.getId(), ProductEventType.BACK_IN_STOCK);
        ProductSubscription s = productSubscriptionRepository.findByProductIdAndUser(product.getId(), user2).orElseThrow();
        Assertions.assertTrue(s.getNotifyOn().contains(ProductEventType.BACK_IN_STOCK));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testRemoveSubscriptionEvent() {
        Userx user2 = userxService.getUserByUsername("user2");
        productSubscriptionService.removeProductSubscriptionEvent(user2, 1000L, ProductEventType.BACK_IN_STOCK);
        ProductSubscription s =  productSubscriptionRepository.findByProductIdAndUser(1000L, user2).orElseThrow();
        Assertions.assertFalse(s.getNotifyOn().contains(ProductEventType.BACK_IN_STOCK));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testDeleteSubscription() {
        Userx user2 = userxService.getUserByUsername("user2");
        productSubscriptionService.deleteProductSubscription(user2, 1000L);
        Assertions.assertTrue(productSubscriptionRepository.findByProductIdAndUser(1000L, user2).isEmpty());
    }

    @Test
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testGetProductSubscription() {
        Userx user = userxService.getUserByUsername("user2");
        ProductSubscription s = productSubscriptionRepository.findByProductIdAndUser(1000L, user).orElseThrow();
        Assertions.assertTrue(s.getNotifyOn().contains(ProductEventType.BACK_IN_STOCK));
        Assertions.assertTrue(s.getNotifyOn().contains(ProductEventType.FOR_SALE));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testUnauthorizedAccess() {
        Userx user2 = userxService.getUserByUsername("user2");
        Assertions.assertThrows(AuthorizationDeniedException.class,() -> productSubscriptionService.addProductSubscription(user2, 1000L, ProductEventType.BACK_IN_STOCK));
        Assertions.assertThrows(AuthorizationDeniedException.class,() -> productSubscriptionService.removeProductSubscriptionEvent(user2, 1000L, ProductEventType.BACK_IN_STOCK));
        Assertions.assertThrows(AuthorizationDeniedException.class,() -> productSubscriptionService.deleteProductSubscription(user2, 1000L));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void testRemoveDeletesSubscription() {
        Userx user = userxService.getUserByUsername("user2");
        productSubscriptionService.removeProductSubscriptionEvent(user, 1000L, ProductEventType.BACK_IN_STOCK);
        productSubscriptionService.removeProductSubscriptionEvent(user, 1000L, ProductEventType.FOR_SALE);
        Assertions.assertTrue(productSubscriptionRepository.findByProductIdAndUser(1000L, user).isEmpty());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "manager", authorities = "MANAGER")
    public void testDiscountNotification() {
        Userx user2 = userxService.getUserByUsername("user2");
        Product p =  productService.loadProduct(1000L).orElseThrow();
        p.setDiscount(0.9);
        productService.saveProduct(p);
        verify(emailNotifier, times(1)).send(anyString(), eq(user2));
        verify(smsNotifier, times(1)).send(anyString(), eq(user2));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testBackInStockNotification() {
        Userx user2 = userxService.getUserByUsername("user2");
        Product p =  productService.loadProduct(1000L).orElseThrow();
        productService.reserveStock(p.getId(), p.getStock());
        productService.saveProduct(p);
        verify(emailNotifier, times(1)).send(anyString(), eq(user2));
        verify(smsNotifier, times(1)).send(anyString(), eq(user2));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testOnDeleteUserDeletesSubscription() {
        Userx user = userxService.getUserByUsername("user2");
        userxService.deleteUser(user);
        Assertions.assertTrue(productSubscriptionRepository.findByProductIdAndUser(1000L, user).isEmpty());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testOnDeleteProductDeletesSubscription() {
        Userx user = userxService.getUserByUsername("user2");
        Product p =  productService.loadProduct(1000L).orElseThrow();
        productService.deleteProduct(p);
        Assertions.assertTrue(productSubscriptionRepository.findByProductIdAndUser(1000L, user).isEmpty());
    }

    @Test
    @WithMockUser(username = "user2", authorities = {"CUSTOMER"})
    public void validateArgsIllegalArgument() {
        Userx user = userxService.getUserByUsername("user2");

        Assertions.assertThrows(IllegalArgumentException.class, () -> productSubscriptionService.createProductSubscription(null, null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> productSubscriptionService.createProductSubscription(user, null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> productSubscriptionService.createProductSubscription(user, 1000L, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> productSubscriptionService.deleteProductSubscription(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> productSubscriptionService.deleteProductSubscription(user, null));
    }
}
