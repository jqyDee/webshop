package at.qe.skeleton.services;

import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.CartItemRepository;
import at.qe.skeleton.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderService(CartItemRepository cartItemRepository, CartService cartService, OrderRepository orderRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    /**
     * Create an order with all products
     *
     * @param currentUser is the user creating the order
     * @return the order created
     */

    @Transactional
    public Order createOrder(Userx currentUser) {
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        Collection<OrderItem> orderItems = convertToOrderItems(currentUser);

        for (OrderItem orderItem : orderItems) {
            boolean allInStock = productService.reserveStock(orderItem.getProduct().getId(), orderItem.getQuantity());
            if (!allInStock) {
                throw new RuntimeException(); // throw custom outOfStockExeption
            }
            order.addProduct(orderItem);
        }
        saveOrder(order);
        return order;
    }

    /**
     * Get all CartItems from User and convert to OrderItems
     *
     * @param currentUser is the user placing the order
     * @return List of orderItems which should be added to the order
     */
    public Collection<OrderItem> convertToOrderItems(Userx currentUser) {
        Collection<CartItem> cartItems = cartService.getCartItems(currentUser);
        Collection<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setName(cartItem.getProduct().getName());
            orderItem.setPrice(cartItem.getProduct().getPrice(), cartItem.getProduct().getDiscount());
            orderItem.setProduct(cartItem.getProduct());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    public Order saveOrder(Order order) {
        return this.orderRepository.save(order);
    }
}
