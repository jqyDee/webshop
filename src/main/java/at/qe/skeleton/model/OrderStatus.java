package at.qe.skeleton.model;

import java.util.Arrays;
import java.util.Collection;

public enum OrderStatus {
    PENDING,
    PENDING_PAYMENT,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean isCancellable() {
        return this.ordinal() <= PENDING_PAYMENT.ordinal();
    }

    static public Collection<OrderStatus> getStaleOrderStatuses() {
        return Arrays.stream(OrderStatus.values())
                     .filter(orderStatus -> orderStatus.ordinal() < PROCESSING.ordinal()).toList();
    }
}
