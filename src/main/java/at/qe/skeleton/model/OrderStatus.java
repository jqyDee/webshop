package at.qe.skeleton.model;

import java.util.Arrays;
import java.util.Collection;

public enum OrderStatus {
    PENDING,
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    static public OrderStatus getCancelThreshold() {
        return PAID;
    }

    public boolean isCancellable() {
        return this.ordinal() <= getCancelThreshold().ordinal();
    }

    static public Collection<OrderStatus> getStaleOrderStatuses() {
        return Arrays.stream(OrderStatus.values())
                     .filter(orderStatus -> orderStatus.ordinal() < getCancelThreshold().ordinal()).toList();
    }
}
