package at.qe.skeleton.services;

import at.qe.skeleton.exceptions.OutOfStockExeption;
import at.qe.skeleton.model.*;
import org.springframework.security.access.AccessDeniedException;
import at.qe.skeleton.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * @param currentUser is the user calling the method
     *if CUSTOMER calls:
     * return all Orders of the speciic Customer
     * if ADMIN or MANAGER calls:
     * return all orders existing at the moment
     */
    public Page<Order> getOrders(Userx currentUser, Pageable pageable) {
        if (currentUser.getRoles().contains(UserxRole.CUSTOMER)) {
            return orderRepository.findAllByUserId(currentUser.getId(), pageable);
        }
        else return orderRepository.findAll(pageable);
    }

    /**
     * Create an order with all products
     * also delete all cartItems after Order was created successfully
     * @param currentUser is the user creating the order
     * @return the order created
     * @throws IllegalStateException if Users cart is empty
     */

    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    @Transactional
    public Order createOrder(Userx currentUser) {
        // Get all cartItems from user
        Collection<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("No cart items found. Cannot create order.");
        }

        Collection<OrderItem> orderItems = convertAndReserveStock(cartItems);

        //create Order and add all products
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        for (OrderItem orderItem : orderItems) {
            order.addProduct(orderItem);
        }

        //save Order and clear users shopping cart
        cartService.clearCartItems(currentUser);
        return saveOrder(order);
    }

    /**
     * Convert CartItems to OrderItems and reserve Stock and
     * @param cartItems The cartItems of the currentUser
     * @return Collection of OrderItems to add to the Order
     * @throws OutOfStockExeption if Item is out of Stock
      */

    private Collection<OrderItem> convertAndReserveStock(Collection<CartItem> cartItems) {

        Collection<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            boolean allInStock = productService.reserveStock(cartItem.getProduct().getId(), cartItem.getQuantity());
            if (!allInStock) {
                throw new OutOfStockExeption(cartItem.getProduct().getName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setName(cartItem.getProduct().getName());
            orderItem.setPrice(cartItem.getProduct().getPrice(), cartItem.getProduct().getDiscount());
            orderItem.setProduct(cartItem.getProduct());
            orderItems.add(orderItem);
        }
        return orderItems;
    }


    private Order saveOrder(Order order) {
        return this.orderRepository.save(order);
    }


    /*<
     *Cancel order before payment and release Stock
     * @param order which should be canceld
     * @return the updated order entity
     * @throws AccessDeniedExeption if User is not allowed to cancel (not his own nor ADMIN nor MANAGER)
     * @throws IllegalStateExeption if the order Status is not <= PendingPayment
     */

    @Transactional
    public void cancelOrder(Order orderToBeCanceled, Userx user) {

        assert orderToBeCanceled.getId() != null;
        Order order = orderRepository.findById(orderToBeCanceled.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().equals(user) && user.getRoles().contains(UserxRole.CUSTOMER)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        if (order.getStatus().isCancellable()) {
            order.setStatus(OrderStatus.CANCELLED);
            for (OrderItem orderItem : order.getProducts()) {
                productService.unreserveStock(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
            orderRepository.save(order);
        }
        else {
            throw new IllegalStateException("Can't cancel order. Order status is not <= PENDING_PAYMENT.");
        }
    }

    public Order confirmOrder(Long orderID, Userx user) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException("You do not have permission to confirm this order");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new IllegalStateException("Can't confirm order. Order status is not PENDING_PAYMENT.");
        }

        order.setStatus(OrderStatus.PROCESSING);
        return orderRepository.save(order);
    }
}
