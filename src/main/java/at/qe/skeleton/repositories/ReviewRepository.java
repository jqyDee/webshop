package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing {@link Review} entities.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
}
