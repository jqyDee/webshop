package at.qe.skeleton.tests;

import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import at.qe.skeleton.repositories.*;
import jakarta.persistence.EntityManager;
import org.springframework.security.access.AccessDeniedException;
import at.qe.skeleton.model.*;
import at.qe.skeleton.services.OrderService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserxRepository userxRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager entityManager;

    private Userx customer1;
    private Userx customer2;
    private Userx customer3;
    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    public void setup() {
        this.customer1 = userxRepository.findFirstByUsername("jonny").orElseThrow();
        this.customer2 = userxRepository.findFirstByUsername("user1").orElseThrow();
        this.customer3 = userxRepository.findFirstByUsername("elvis").orElseThrow();
    }

    @Test
    @Transactional
    @DirtiesContext
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testOrderDataInitialization() {

        Order order = new Order();
        order.setUser(customer1);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        Page<Order> orders = orderService.getOrders(customer1, PageRequest.of(0, 10));
        assertEquals(3, orders.getTotalElements());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testLoadOrder_ExistingIds() {

        Optional<Order> order8000 = orderService.loadOrder(8000L);
        assertTrue(order8000.isPresent(), "Order should exist");
        assertEquals(8000L, order8000.get().getId());

        Optional<Order> order7000 = orderService.loadOrder(7000L);
        assertTrue(order7000.isPresent(), "Order should exist");

        Optional<Order> order9000 = orderService.loadOrder(9000L);
        assertTrue(order9000.isPresent(), "Order should exist");
    }

    @Test
    @WithMockUser(username = "admin")
    void testLoadOrder_NonExistingId() {
        Optional<Order> result = orderService.loadOrder(9999L);
        assertFalse(result.isPresent(), "Order mit ID 9999 sollte nicht existieren");
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testCreateOrderFromCartItems() {
        Product product = productRepository.findById(5000L).orElseThrow();
        int stockBeforeOrder = product.getStock();

        Order newOrder = orderService.createOrder(customer1);

        Assertions.assertNotNull(newOrder.getId(), "Order should have a generated ID");
        assertEquals(customer1.getId(), newOrder.getUser().getId());

        double expectedSum = product.getPrice() * 3;
        assertEquals(expectedSum, newOrder.getSum(), "Price must be calculated correctly");

        Collection<CartItem> remainingCartItems = cartItemRepository.findAllByUser(customer1);
        assertTrue(remainingCartItems.isEmpty(), "Cart should be cleared after order creation");

        Product updatedProduct = productRepository.findById(5000L).orElseThrow();
        assertEquals(stockBeforeOrder-3, updatedProduct.getStock(), "Stock should be updated");
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testNotInStock() {
        Assertions.assertThrows(OutOfStockException.class, () -> orderService.createOrder(customer2));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testCancelOrder() {
        Order orderToCancel = orderRepository.findById(9000L).orElseThrow();
        Userx user = userxRepository.findFirstByUsername("jonny").orElseThrow();
        Product product = productRepository.findById(5000L).orElseThrow();

        int stockBeforeCancel = product.getStock();
        int quantityToReturn = 2;

        orderService.cancelOrder(orderToCancel, user);
        assertEquals(OrderStatus.CANCELLED, orderToCancel.getStatus());
        Product updatedProduct = productRepository.findById(5000L).orElseThrow();
        assertEquals(stockBeforeCancel + quantityToReturn, updatedProduct.getStock());

        orderItemRepository.flush();
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testConfirmOrder() {
        Order orderToConfirm = orderRepository.findById(9000L).orElseThrow();
        Userx user = userxRepository.findFirstByUsername("jonny").orElseThrow();
        Address deliveryAddress = user.getShippingAddress();
        Address paymentAddress = user.getPaymentAddress();

        orderService.confirmOrder(orderToConfirm, user, deliveryAddress, paymentAddress);
        Order updatedOrder = orderRepository.findById(9000L).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
        assertEquals(deliveryAddress.getStreet(), updatedOrder.getShippingAddress().getStreet(),
                "Delivery address should be correct");
        assertEquals(paymentAddress.getStreet(), updatedOrder.getPaymentAddress().getStreet(),
                "Payment address should be correct");
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testGetOrdersUser() {
        Page<Order> orders = orderService.getOrders(customer1, PageRequest.of(0, 10));
        assertEquals(2, orders.getTotalElements());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin2", authorities = {"ADMIN"})
    public void testGetOrdersAdmin() {
        Page<Order> orders = orderService.getAllOrders(PageRequest.of(0, 10));
        assertEquals(3, orders.getTotalElements());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testCancelOrderUnauthorized() {
        Order order = orderRepository.findById(9000L).orElseThrow();
        Userx user = userxRepository.findFirstByUsername("user2").orElseThrow();
        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder(order, user));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testCartEmptyCreateOrder() {
        Assertions.assertThrows(CartEmptyException.class, () -> orderService.createOrder(customer3));
    }

    @Transactional
    public void backdateOrder(Long orderId, int minutesToSubtract) {

        entityManager.createNativeQuery("UPDATE orders SET created_date = ?1 WHERE id = ?2")
                     .setParameter(1, LocalDateTime.now().minusMinutes(minutesToSubtract))
                     .setParameter(2, orderId)
                     .executeUpdate();

        entityManager.clear();
    }

    @Test
    @Transactional
    @DirtiesContext
    @WithMockUser(username = "admin2", authorities = {"ADMIN"})
    public void testCleanupStaleOrders() {
        Order staleOrder = new Order();
        staleOrder.setUser(customer1);
        staleOrder.setStatus(OrderStatus.PENDING);
        orderRepository.save(staleOrder);

        backdateOrder(staleOrder.getId(), 45);
        Assertions.assertNotNull(staleOrder.getId());
        Order o1 = orderRepository.findById(staleOrder.getId()).orElseThrow();
        Assertions.assertSame(OrderStatus.PENDING, o1.getStatus());

        orderService.cleanupStaleOrders();

        Order updatedOrder = orderRepository.findById(staleOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus(),
                                "Order older than 30 mins should be cancelled");
    }

    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testGetOrdersIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.getOrders(null, null));
    }

    @Test
    public void testLoadOrderIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.loadOrder(null));
    }

    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testCreateOrderIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(null));
    }

    @Test
    public void testCancelOrderIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(null, null));

        Order order = new Order();
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(order, null));
    }

    @Test
    public void testConfirmOrderIllegalArgument() {
        Order order = new Order();
        Userx user = new Userx();
        Address address = new Address();
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(null, null, null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(order, null, null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(order, user, null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(order, user, address, null));
    }

    @Test
    public void testConfirmOrderAccessDenied() {
        Userx userOrder = new Userx();
        userOrder.setId(100L);
        Order order = new Order();
        order.setUser(userOrder);
        Address address = new Address();
        Userx user = new Userx();
        user.setId(200L);
        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.confirmOrder(order, user, address, address));
    }

    @Test
    public void testConfirmIllegalState() {
        Userx user = new Userx();
        user.setId(100L);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        Address address = new Address();
        Assertions.assertThrows(IllegalStateException.class, () -> orderService.confirmOrder(order, user, address, address));
    }

    @Test
    public void testValidateAddressOwnershipAccessDenied() {
        Userx userWrong = new Userx();
        userWrong.setId(100L);
        Userx user = new Userx();
        user.setId(200L);

        Address address = addressRepository.findById(1000L).orElseThrow();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);

        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.confirmOrder(order, user, address, address));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testValidateAddressOwnershipSetOwnership() {
        Userx user = userxRepository.findById(8000L).orElseThrow();

        Address address = new Address();
        address.setCity("London");
        address.setCountry("United States");
        address.setPostalCode("12345");
        address.setNumber("asd");
        address.setStreet("London");

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setCreatedDate(LocalDateTime.now());
        order.setSum(10.0);

        orderService.confirmOrder(order, user, address, address);

        Optional<Address> address2 = addressRepository.findById(1L);
        Assertions.assertTrue(address2.isPresent());
    }
}