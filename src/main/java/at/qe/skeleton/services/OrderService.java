package at.qe.skeleton.services;

import at.qe.skeleton.model.*;
import org.springframework.security.access.AccessDeniedException;
import at.qe.skeleton.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderService(CartService cartService, OrderRepository orderRepository, ProductService productService) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    public Page<Order> getOrdersByUser(Userx user, Pageable pageable) {
        return orderRepository.findAllByUserId(user.getId(), pageable);
    }

    /**
     * Create an order with all products
     * also delete all cartItems after Order was created successfully
     * @param currentUser is the user creating the order
     * @return the order created
     */

    @Transactional
    public Order createOrder(Userx currentUser) {
        // Get all cartItems from user
        Collection<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("No cart items found. Cannot create order.");
        }

        // Reserve Stock and convert CartItems to OrderItems
        Collection<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            boolean allInStock = productService.reserveStock(cartItem.getProduct().getId(), cartItem.getQuantity());
            if (!allInStock) {
                throw new RuntimeException(); // throw custom outOfStockExeption
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setName(cartItem.getProduct().getName());
            orderItem.setPrice(cartItem.getProduct().getPrice(), cartItem.getProduct().getDiscount());
            orderItem.setProduct(cartItem.getProduct());
            orderItems.add(orderItem);
        }

        //create Order and add all products
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        for (OrderItem orderItem : orderItems) {
            order.addProduct(orderItem);
        }

        //save Order and clear users shopping cart
        saveOrder(order);
        cartService.clearCartItems(currentUser);
        return order;
    }

    public Order saveOrder(Order order) {
        return this.orderRepository.save(order);
    }

    /**
     *Cancel order before payment and release Stock
     * @param order which should be canceld
     * @return the updated order entity
     */

    @Transactional
    public Order cancelOrder(Order orderToBeCanceled, Userx user) {
        Order order = orderRepository.findById(orderToBeCanceled.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.CANCELLED);
            for (OrderItem orderItem : order.getProducts()) {
                productService.unreserveStock(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
            return this.orderRepository.save(order);
        }
        else {
            throw new IllegalStateException("Can't cancel order. Order status is not PENDING_PAYMENT.");
        }
    }
}
