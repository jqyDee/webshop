package at.qe.skeleton.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CartEmptyException extends RuntimeException {
    public CartEmptyException() {
        super("Can not create Order. Your Cart is empty.");
    }
}
