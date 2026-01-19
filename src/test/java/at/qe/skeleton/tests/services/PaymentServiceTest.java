package at.qe.skeleton.tests.services;

import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.OrderStatus;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.OrderRepository;
import at.qe.skeleton.repositories.UserxRepository;
import at.qe.skeleton.services.OrderService;
import at.qe.skeleton.services.PaymentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    UserxRepository userxRepository;
    @Autowired
    OrderService orderService;

    private Userx customer1;
    private Userx customer2;
    private Userx customer3;

    @BeforeEach
    public void setup() {
        this.customer1 = userxRepository.findFirstByUsername("jonny").orElseThrow();
        this.customer2 = userxRepository.findFirstByUsername("user1").orElseThrow();
        this.customer3 = userxRepository.findFirstByUsername("elvis").orElseThrow();
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "jonny", authorities = {"CUSTOMER"})
    public void testPaymentReceived() {
        Order order = orderRepository.findById(8000L).orElseThrow();
        paymentService.paymentReceived(order, customer1);

        Order updatedOrder = orderRepository.findById(8000L).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }


    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testPaymentReceivedUnauthorized() {
        Order order = orderRepository.findById(9000L).orElseThrow();
        Assertions.assertThrows(AccessDeniedException.class, () -> paymentService.paymentReceived(order, customer2));
    }

    @Transactional
    @DirtiesContext
    @Test
    @WithMockUser(username = "elvis", authorities = {"CUSTOMER"})
    public void testWrongOrderStatus() {
        Order order = orderRepository.findById(7000L).orElseThrow();
        Assertions.assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order, customer3));
        Assertions.assertThrows(IllegalStateException.class, () -> paymentService.paymentReceived(order, customer3));
    }

    @Test
    public void testPerformPaymentIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> paymentService.performPayment(null));
    }

    @Test
    public void testReversePaymentIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> paymentService.reversePayment(null));
    }

    @Test
    public void testReversePayment() {
        // stub
        Order order = new Order();
        paymentService.reversePayment(order);
    }
}
