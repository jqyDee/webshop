package at.qe.skeleton.exceptions;

/**
 * Exception which is thrown when a cart item is not in stock in the needed quantity
 */
public class OutOfStockException extends RuntimeException {
  public OutOfStockException(String message) {
    super(message);
  }
}
