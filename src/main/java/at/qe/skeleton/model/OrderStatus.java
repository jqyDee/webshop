package at.qe.skeleton.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * Enumeration of available Order status.
 */
public enum OrderStatus {
    PENDING,
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus getCancelThreshold() {
        return PAID;
    }

    public boolean isCancellable() {
        return this.ordinal() <= getCancelThreshold().ordinal();
    }

    public static Collection<OrderStatus> getStaleOrderStatuses() {
        return Arrays.stream(OrderStatus.values())
                     .filter(orderStatus -> orderStatus.ordinal() < getCancelThreshold().ordinal()).toList();
    }
}
