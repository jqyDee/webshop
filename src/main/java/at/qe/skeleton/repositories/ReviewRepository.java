package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link Review} entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>,
                                          JpaSpecificationExecutor<Review> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
}
