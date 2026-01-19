package at.qe.skeleton.services;

import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.OrderStatus;
import at.qe.skeleton.model.Userx;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
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
     */
    @Transactional
    public void paymentReceived(Order order, Userx user) {
        if (!order.getUser().equals(user)) {
            throw new AccessDeniedException("You do not have permission to confirm this order");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new IllegalStateException("Can't confirm order. Order status is not PENDING_PAYMENT.");
        }

        order.setStatus(OrderStatus.PAID);
        // todo shipping,... all stubed
        order.setStatus(OrderStatus.DELIVERED);
    }

    public void reversePayment(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null");
        }
        System.out.println("Payment reversed");
    }

}
