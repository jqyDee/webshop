package at.qe.skeleton.tests;

import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import org.springframework.security.access.AccessDeniedException;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.CartItemRepository;
import at.qe.skeleton.repositories.OrderRepository;
import at.qe.skeleton.repositories.ProductRepository;
import at.qe.skeleton.repositories.UserxRepository;
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

import java.util.*;


@SpringBootTest
class OrderServiceTest {


    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserxRepository userxRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Userx customer1;
    private Userx customer2;
    private Userx customer3;
    private Userx admin;

    @BeforeEach
    public void setup() {
        this.customer1 = userxRepository.findFirstByUsername("jonny").orElseThrow();
        this.customer2 = userxRepository.findFirstByUsername("user1").orElseThrow();
        this.customer3 = userxRepository.findFirstByUsername("elvis").orElseThrow();
        this.admin = userxRepository.findFirstByUsername("admin2").orElseThrow();
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
        Assertions.assertEquals(3, orders.getTotalElements());
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
        Assertions.assertEquals(customer1.getId(), newOrder.getUser().getId());

        double expectedSum = product.getPrice() * 3;
        Assertions.assertEquals(expectedSum, newOrder.getSum(), "Price msut be calculated correctly");

        Collection<CartItem> remainingCartItems = cartItemRepository.findAllByUser(customer1);
        Assertions.assertTrue(remainingCartItems.isEmpty(), "Cart should be cleared after order creation");

        Product updatedProduct = productRepository.findById(5000L).orElseThrow();
        Assertions.assertEquals(stockBeforeOrder-3, updatedProduct.getStock(), "Stock should be updated");
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
        Assertions.assertEquals(OrderStatus.CANCELLED, orderToCancel.getStatus());
        Product updatedProduct = productRepository.findById(5000L).orElseThrow();
        Assertions.assertEquals(stockBeforeCancel + quantityToReturn, updatedProduct.getStock());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testPaymentReceived() {
        Order order = orderRepository.findById(8000L).orElseThrow();
        Order updatedOrder = orderService.paymentReceived(order, customer1);

        Assertions.assertEquals(OrderStatus.PROCESSING, updatedOrder.getStatus());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testGetOrdersUser() {
        Page<Order> orders = orderService.getOrders(customer1, PageRequest.of(0, 10));
        Assertions.assertEquals(2, orders.getTotalElements());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin2", authorities = {"ADMIN"})
    public void testGetOrdersAdmin() {
        Page<Order> orders = orderService.getOrders(admin, PageRequest.of(0, 10));
        Assertions.assertEquals(3, orders.getTotalElements());
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testPaymentReceivedUnauthorized() {
        Order order = orderRepository.findById(9000L).orElseThrow();
        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.paymentReceived(order, customer2));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testCancelOrderUnauthorized() {
        Order order = orderRepository.findById(9000L).orElseThrow();
        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder(order, customer2));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testCartEmptyCreateOrder() {
        Assertions.assertThrows(CartEmptyException.class, () -> orderService.createOrder(customer3));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testWrongOrderStatus() {
        Order order = orderRepository.findById(7000L).orElseThrow();
        Assertions.assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order, customer3));
        Assertions.assertThrows(IllegalStateException.class, () -> orderService.paymentReceived(order, customer3));
    }
}