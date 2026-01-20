package at.qe.skeleton.services;

import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.AddressRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import at.qe.skeleton.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AddressRepository addressRepository;
    private final PaymentService paymentService;

    @Autowired
    public OrderService(CartService cartService, OrderRepository orderRepository, ProductService productService,
                        AddressRepository addressRepository, PaymentService paymentService) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.addressRepository = addressRepository;
        this.paymentService = paymentService;
    }

    /**
     * Scheduled Task to clean up stale orders in the database.
     * Default time interval is set to 10 minutes (600.000ms) but can be set as application
     * property.
     */
    @Scheduled(fixedRateString = "${order.cleanup.rate}")
    @Transactional
    public void cleanupStaleOrders() {
        int cutoff = 30; // cutoff time in minutes
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(cutoff);

        Collection<Order> staleOrders = orderRepository.findAllByStatusInAndCreatedDateBefore(
                OrderStatus.getStaleOrderStatuses(), threshold);

        staleOrders.forEach(this::cancelStaleOrder);
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
            throw new IllegalArgumentException("currentUser is null");
        }

        if (currentUser.getRole().equals(UserxRole.CUSTOMER)) {
            return orderRepository.findAllByUserId(currentUser.getId(), pageable);
        }

        return Page.empty(pageable);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Search for order with id in database.
     *
     * @param id id to search in the database
     * @return order matching the id
     */
    public Optional<Order> loadOrder(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }

        return this.orderRepository.findById(id);
    }

    /**
     * Create an order with all products. Also delete all cartItems after Order was created
     * successfully
     *
     * @param currentUser is the user creating the order
     * @return the order created
     * @throws IllegalArgumentException if User is null
     * @throws OutOfStockException if cart item is out of stock
     * @throws CartEmptyException if cart is empty
     */
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Transactional
    public Order createOrder(Userx currentUser) throws IllegalArgumentException, OutOfStockException, CartEmptyException {
        if (currentUser == null) {
            throw new IllegalArgumentException("User is null");
        }

        // Get all cartItems from user
        Collection<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        Collection<OrderItem> orderItems = convertAndReserveStock(cartItems.stream().toList());

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
    private Collection<OrderItem> convertAndReserveStock(List<CartItem> cartItems)
            throws OutOfStockException {
        Collection<OrderItem> orderItems = new ArrayList<>();

        boolean allInStock = true;

        for (CartItem cartItem : cartItems) {
            allInStock = productService.reserveStock(cartItem.getProduct().getId(), cartItem.getQuantity());

            if (!allInStock) {
                break;
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setName(cartItem.getProduct().getName());
            orderItem.setTotal(cartItem.getProduct().getDiscountedPrice());
            orderItem.setProduct(cartItem.getProduct());
            orderItems.add(orderItem);
        }

        if (!allInStock) {
            for (OrderItem orderItem : orderItems) {
                productService.releaseStock(orderItem);
            }
            throw new OutOfStockException("x");
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
            throw new IllegalArgumentException("Order and user cannot be null");
        }

        Order order = orderRepository.findById(orderToBeCanceled.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().equals(user) && user.getRole().equals(UserxRole.CUSTOMER)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        if (!order.getStatus().isCancellable()) {
            throw new IllegalStateException(
                    "Can't cancel order. Order status is not <= PAID.");
        }

        if (OrderStatus.PAID.equals(order.getStatus())) {
            paymentService.reversePayment(order);
        }

        order.setStatus(OrderStatus.CANCELLED);
        for (OrderItem orderItem : order.getProducts()) {
            productService.releaseStock(orderItem);
        }
        orderRepository.save(order);
    }

    /**
     * Cancels a stale order
     *
     * @param order stale order to be cancelled
     */
    private void cancelStaleOrder(Order order) {
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
     * @throws IllegalArgumentException if order user or address is null
     * @throws AccessDeniedException if the user did not place the order or the addresses do not belong to the user.
     * @throws IllegalStateException if order status is not valid to be confirmed
     */
    @Transactional
    public Order confirmOrder(Order order, Userx user, Address shippingAddress,
                              Address paymentAddress)
            throws IllegalArgumentException, AccessDeniedException, IllegalStateException {
        if (order == null || user == null || shippingAddress == null || paymentAddress == null) {
            throw new IllegalArgumentException("Order or Userx or addresses cannot be null");
        }

        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException("You do not have permission to confirm this order");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("Can't confirm order. Order status is not PENDING.");
        }

        validateAddressOwnership(shippingAddress, user);
        validateAddressOwnership(paymentAddress, user);

        order.setShippingAddress(orderAddressCreation(shippingAddress));
        order.setPaymentAddress(orderAddressCreation(paymentAddress));

        order.setStatus(OrderStatus.PENDING_PAYMENT);

        boolean paymentSuccessful = paymentService.performPayment(order);

        if (paymentSuccessful) {
            paymentService.paymentReceived(order, user);
        }
        // This will never be reached in the current state, as the payment is stubbed to always succeed
        return orderRepository.save(order);
    }

    /**
     * Create a copy of an address to make sure it is not changed once an order is confirmed
     *
     * @param address address to copy
     * @return copied address
     */
    private Address orderAddressCreation(Address address) {
        Address thisAddress = new Address();
        thisAddress.setStreet(address.getStreet());
        thisAddress.setNumber(address.getNumber());
        thisAddress.setPostalCode(address.getPostalCode());
        thisAddress.setCity(address.getCity());
        thisAddress.setCountry(address.getCountry());
        thisAddress.setUser(null);
        return thisAddress;
    }

    /**
     * Validate the user of an address
     *
     * @param address address to check
     * @param user user to check agains
     * @throws AccessDeniedException if address is not owned by user
     * @throws IllegalArgumentException if address cannot be found in database
     */
    private void validateAddressOwnership(Address address, Userx user)
            throws AccessDeniedException, IllegalArgumentException {

        if (address.getId() != null) {
            Address existingAddress = addressRepository.findById(address.getId())
                                                       .orElseThrow(() -> new IllegalArgumentException("Address not found"));

            if (!existingAddress.getUser().equals(user)) {
                throw new AccessDeniedException("You do not own this address.");
            }
        } else {
            address.setUser(user);
        }
    }
}
