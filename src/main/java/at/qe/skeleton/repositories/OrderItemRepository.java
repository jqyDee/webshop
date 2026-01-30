package at.qe.skeleton.repositories;

import at.qe.skeleton.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing {@link OrderItem} entities.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
