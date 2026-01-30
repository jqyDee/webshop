package at.qe.skeleton.exceptions;

/**
 * Exception which is thrown when Cart is empty
 */
public class CartEmptyException extends RuntimeException {
    public CartEmptyException() {
        super("Can not create Order. Your Cart is empty.");
    }
}
