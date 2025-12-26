package at.qe.skeleton.exceptions;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException() {
        super("Can not create Order. Your Cart is empty.");
    }
}
