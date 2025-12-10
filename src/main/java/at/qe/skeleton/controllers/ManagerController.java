package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    public ManagerController() {
    }

    @PostMapping("/createProduct")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO product) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PatchMapping("/product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, ProductDTO productUpdateDTO) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @GetMapping("/orders")
    public ResponseEntity<PageableListDTO<OrderDTO>> getAllOrders() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<Void> updateOrder(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
