package at.qe.skeleton.exceptions;

public class CartEmptyExeption extends RuntimeException {
    public CartEmptyExeption() {
        super("Can not create Order. Your Cart is empty.");;
    }
}
