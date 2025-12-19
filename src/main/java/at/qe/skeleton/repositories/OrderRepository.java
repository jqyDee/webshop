package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>,
                                        JpaSpecificationExecutor<Order> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}