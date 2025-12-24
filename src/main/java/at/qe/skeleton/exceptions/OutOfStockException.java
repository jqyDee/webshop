package at.qe.skeleton.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OutOfStockException extends RuntimeException {
  public OutOfStockException(String productName) {
    super("Product " + productName + " is out of stock. Please try again.");
  }
}
