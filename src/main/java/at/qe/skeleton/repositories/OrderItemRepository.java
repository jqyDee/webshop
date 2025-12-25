package at.qe.skeleton.repositories;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
