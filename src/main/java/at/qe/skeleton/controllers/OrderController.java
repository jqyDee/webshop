package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.dtos.OrderResponseDTO;
import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.model.Userx;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    public OrderController() {

    }

    @GetMapping("")
    public ResponseEntity<PageableListDTO<OrderDTO>> getOrders(@AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //What do we need this method1 for?
    @PatchMapping("")
    public ResponseEntity<OrderDTO> updateOrder(@AuthenticationPrincipal Userx user,
                                                OrderDTO orderDTO) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponseDTO> createOrder(@AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("/confirmOrder")
    public ResponseEntity<OrderDTO> confirmOrder(@RequestBody OrderDTO order, @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PostMapping("/cancelOrder")
    public ResponseEntity<OrderDTO> cancelOrder(@RequestBody OrderDTO order, @AuthenticationPrincipal Userx user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
