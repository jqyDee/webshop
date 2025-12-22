package at.qe.skeleton.services;

import at.qe.skeleton.exceptions.OutOfStockExeption;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private CartService cartService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Userx customer;
    private Userx admin;
    private Product product;
    private CartItem cartItem;


    @BeforeEach
    void setUp() {
        customer = new Userx();
        customer.setId(1L);
        customer.setRoles(Set.of(UserxRole.CUSTOMER));

        admin = new Userx();
        admin.setId(2L);
        admin.setRoles(Set.of(UserxRole.ADMIN));

        product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setDiscount(0.0);

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetOrdersAsCustomer() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAllByUserId(customer.getId(), pageable)).thenReturn(Page.empty());

        orderService.getOrders(customer, pageable);

        verify(orderRepository).findAllByUserId(customer.getId(), pageable);
        verify(orderRepository, never()).findAll(pageable);
    }

    @Test
    void testGetOrdersAsAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(Page.empty());

        orderService.getOrders(admin, pageable);

        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never()).findAllByUserId(anyLong(), any());
    }

    @Test
    void createOrderSuccess() {
        when(cartService.getCartItems(customer)).thenReturn(List.of(cartItem));
        when(productService.reserveStock(anyLong(), anyInt())).thenReturn(true);

        Order result = orderService.createOrder(customer);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCartItems(customer);
    }

    @Test
    void createOrderWithEmptyCart() {
        when(cartService.getCartItems(customer)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(customer));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderOutOfStock() {
        when(cartService.getCartItems(customer)).thenReturn(List.of(cartItem));
        when(productService.reserveStock(anyLong(), anyInt())).thenReturn(false);

        assertThrows(OutOfStockExeption.class, () -> orderService.createOrder(customer));
    }

    @Test
    void cancelOrderNotOwner() {
        Userx otherCustomer = new Userx();
        otherCustomer.setId(99L);
        otherCustomer.setRoles(Set.of(UserxRole.CUSTOMER));

        Order order = new Order();
        order.setId(5L);
        order.setUser(otherCustomer);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder(order, customer));
    }

    @Test
    void cancelOrderSuccess() {
        Order order = new Order();
        order.setId(5L);
        order.setUser(customer);
        order.setStatus(OrderStatus.PENDING_PAYMENT); // Wichtig für isCancellable()

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        order.addProduct(item);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.cancelOrder(order, customer);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(productService).unreserveStock(product.getId(), 2);
    }

    @Test
    void confirmOrder_Success_ShouldSetToProcessing() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(customer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.confirmOrder(1L, customer);

        assertEquals(OrderStatus.PROCESSING, result.getStatus());
    }
}