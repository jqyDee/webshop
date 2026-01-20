package at.qe.skeleton.services;

import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.OrderStatus;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;

    @Autowired
    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Perform the payment (STUBBED)
     *
     * @param order the order the user just confirmed and wants to pay
     * @return boolean whether the payment went through or not, in our case always true as payment only stubbed
     */
    public boolean performPayment(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null");
        }

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

        order.setStatus(OrderStatus.PAID);
        // todo shipping,... all stubed
        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);

    }

    public void reversePayment(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null");
        }
        System.out.println("Payment reversed");
    }

}
