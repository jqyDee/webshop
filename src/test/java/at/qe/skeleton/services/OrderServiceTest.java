package at.qe.skeleton.services;

import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.CartItemRepository;
import at.qe.skeleton.repositories.OrderRepository;
import at.qe.skeleton.repositories.ProductRepository;
import at.qe.skeleton.repositories.UserxRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.test.context.support.WithMockUser;

import java.util.*;


@SpringBootTest
class OrderServiceTest {


    @Autowired
    private OrderService orderService;

    @Autowired
    private UserxService userxService;

    private Userx testCustomer;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserxRepository userxRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Userx customer1;
    @Autowired
    private ProductService productService;

    @BeforeEach
    public void setup() {
        this.customer1 = userxRepository.getByUsername("user1");
    }

    @Test
    @Transactional
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testOrderDataInitialization() {

        Address address = new Address();
        address.setNumber("1");
        address.setStreet("Street");
        address.setCity("City");
        address.setCountry("Country");
        address.setPostalCode("jjjj");

        Order order = new Order();
        order.setUser(customer1);
        order.setPaymentAddress(address);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        Page<Order> orders = orderService.getOrders(customer1, PageRequest.of(0, 10));
        Assertions.assertEquals(1, orders.getTotalElements());
    }


    @Transactional
    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testCreateOrderFromCartItems() {
        Product product = productRepository.findById(1000L).get();
        CartItem cartItem = new CartItem();
        cartItem.setUser(customer1);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        double stockBeforeOrder = product.getStock();

        cartItemRepository.save(cartItem);
        Order newOrder = orderService.createOrder(customer1);

        Assertions.assertNotNull(newOrder.getId(), "Order should have a generated ID");
        Assertions.assertEquals(customer1.getId(), newOrder.getUser().getId());

        double expectedSum = product.getPrice() * 3;
        Assertions.assertEquals(expectedSum, newOrder.getSum(), "Price msut be calculated correctly");

        Collection<CartItem> remainingCartItems = cartItemRepository.findAllByUser(customer1);
        Assertions.assertTrue(remainingCartItems.isEmpty(), "Cart should be cleared after order creation");

        Product updatedProduct = productRepository.findById(1000L).get();
        Assertions.assertEquals(stockBeforeOrder-3, updatedProduct.getStock(), "Stock should be updated");
    }

}