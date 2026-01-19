package at.qe.skeleton.tests;

import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.OrderStatus;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.OrderRepository;
import at.qe.skeleton.repositories.UserxRepository;
import at.qe.skeleton.services.OrderService;
import jakarta.persistence.EntityManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class OrderServiceScheduleTest {
    @MockitoSpyBean
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserxRepository userxRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Userx customer1;

    @BeforeEach
    public void setup() {
        this.customer1 = userxRepository.findFirstByUsername("jonny").orElseThrow();
    }

    public void backdateOrder(Long orderId, int minutesToSubtract) {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createNativeQuery("UPDATE orders SET created_date = ?1 WHERE id = ?2")
                         .setParameter(1, LocalDateTime.now().minusMinutes(minutesToSubtract))
                         .setParameter(2, orderId)
                         .executeUpdate();

            entityManager.clear();
        });
    }

    @Test
    @DirtiesContext
    public void testOrderServiceSchedulerExecution() {
        Order staleOrder = new Order();
        staleOrder.setUser(customer1);
        staleOrder.setStatus(OrderStatus.PENDING);
        orderRepository.saveAndFlush(staleOrder);

        backdateOrder(staleOrder.getId(), 45);

        Assertions.assertNotNull(staleOrder.getId());
        Order check = orderRepository.findById(staleOrder.getId()).orElseThrow();
        Assertions.assertEquals(OrderStatus.PENDING, check.getStatus());

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                  .untilAsserted(() -> {
                      Mockito.verify(orderService, Mockito.atLeastOnce()).cleanupStaleOrders();
                      Order updatedOrder = orderRepository.findById(staleOrder.getId()).orElseThrow();
                      Assertions.assertSame(OrderStatus.CANCELLED, updatedOrder.getStatus());
                  });
    }
}
