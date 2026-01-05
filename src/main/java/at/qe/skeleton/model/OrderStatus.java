package at.qe.skeleton.model;

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
}
