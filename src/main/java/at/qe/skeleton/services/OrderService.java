package at.qe.skeleton.services;

import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
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
     * Get page of all orders or customers orders
     *
     * @param currentUser is the user calling the method
     * @return page of orders. If Customer calls, then Customers orders. If Admin/Manager calls
     *         then all orders.
     */
    @PreAuthorize("isAuthenticated()")
    public Page<Order> getOrders(Userx currentUser, Pageable pageable) {
        if (currentUser == null) {
            return Page.empty();
        }

        if (currentUser.getRoles().contains(UserxRole.CUSTOMER)) {
            return orderRepository.findAllByUserId(currentUser.getId(), pageable);
        }

        return orderRepository.findAll(pageable);
    }

    /**
     * Create an order with all products. Also delete all cartItems after Order was created
     * successfully
     *
     * @param currentUser is the user creating the order
     * @return the order created
     * @throws IllegalStateException if Users cart is empty
     * @throws OutOfStockException if cart item is out of stock
     * @throws CartEmptyException if cart is empty
     */
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Transactional
    public Order createOrder(Userx currentUser) throws IllegalStateException, OutOfStockException, CartEmptyException {
        if (currentUser == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Get all cartItems from user
        Collection<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        Collection<OrderItem> orderItems = convertAndReserveStock(cartItems);

        // create Order and add all products
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING);
        for (OrderItem orderItem : orderItems) {
            order.addProduct(orderItem);
        }

        // save Order and clear users shopping cart
        cartService.clearCartItems(currentUser);
        return saveOrder(order);
    }

    /**
     * Convert CartItems to OrderItems and reserve Stock and
     *
     * @param cartItems The cartItems of the currentUser
     * @return Collection of OrderItems to add to the Order
     * @throws OutOfStockException if cart item is out of Stock
     */
    private Collection<OrderItem> convertAndReserveStock(Collection<CartItem> cartItems)
            throws OutOfStockException {
        Collection<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            boolean allInStock = productService.reserveStock(cartItem.getProduct().getId(), cartItem.getQuantity());
            if (!allInStock) {
                throw new OutOfStockException(cartItem.getProduct().getName());
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


    /**
     * Save order to database
     *
     * @param order order to be saved
     * @return the saved order
     */
    private Order saveOrder(Order order) {
        return this.orderRepository.save(order);
    }


    /**
     * Cancel order before payment and release stock
     *
     * @param orderToBeCanceled order which should be cancelled
     * @throws AccessDeniedException if user is not allowed to cancel (not his own nor ADMIN nor MANAGER)
     * @throws IllegalStateException if order is not cancellable anymore
     */
    @Transactional
    public void cancelOrder(Order orderToBeCanceled, Userx user) {
        if (orderToBeCanceled == null || orderToBeCanceled.getId() == null || user == null) {
            return;
        }

        Order order = orderRepository.findById(orderToBeCanceled.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().equals(user) && user.getRoles().contains(UserxRole.CUSTOMER)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        if (!order.getStatus().isCancellable()) {
            throw new IllegalStateException(
                    "Can't cancel order. Order status is not <= PENDING_PAYMENT.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        for (OrderItem orderItem : order.getProducts()) {
            productService.releaseStock(orderItem);
        }
        order.getProducts().clear();
        orderRepository.save(order);
    }

    /**
     * Change order status and set address when customer confirms order
     *
     * @param order, the order the user just created
     * @param user the user who is placing an order
     * @param shippingAddress address user has saved or just enter
     * @param paymentAddress address user has saved or just enter
     */
    @Transactional
    public void confirmOrder(Order order, Userx user, Address shippingAddress, Address paymentAddress) {
        if (order == null || user == null || shippingAddress == null || paymentAddress == null) {
            return;
        }

        if (order.getUser() != user) {
            throw new AccessDeniedException("You do not have permission to confirm this order");
        }

        if (shippingAddress.getUser() != user || paymentAddress.getUser() != user) {
            throw new AccessDeniedException("The used addresses do not belong to the orders user");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("Can't confirm order. Order status is not PENDING.");
        }

        order.setShippingAddress(shippingAddress);
        order.setPaymentAddress(paymentAddress);

        order.setStatus(OrderStatus.PENDING_PAYMENT);

        boolean paymentSuccessful = performStubbedPayment(order);

        if (paymentSuccessful) {
            paymentReceived(order, user);
        }
    }

    // TODO: put this in own payment service
    /**
     * Perform the payment (STUBBED)
     *
     * @param order the order the user just confirmed and wants to pay
     * @return boolean whether the payment went through or not, in our case always true as payment only stubbed
     */
    private boolean performStubbedPayment(Order order) {
        System.out.println("Simulate Order payment for Order:  " + order.getId() + " with total amount: " + order.getSum());
        return true;
    }


    /**
     * Set order status after payment received
     *
     * @param order order to be set to payment received
     * @param user currently authenticated user
     * @return the updated order
     */
    @Transactional
    public Order paymentReceived(Order order, Userx user) {
        if (user == null || order == null) {
            throw new IllegalArgumentException("User and Order cannot be null");
        }
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
