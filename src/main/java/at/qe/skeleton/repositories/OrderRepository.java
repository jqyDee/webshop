package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Repository for managing {@link Order} entities.
 */
public interface OrderRepository extends JpaRepository<Order, Long>,
                                        JpaSpecificationExecutor<Order> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Collection<Order> findAllByStatusInAndCreatedDateBefore(Collection<OrderStatus> status, LocalDateTime date);
}