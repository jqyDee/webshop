package at.qe.skeleton.repositories;

import at.qe.skeleton.model.ProductEventType;
import at.qe.skeleton.model.ProductSubscription;
import at.qe.skeleton.model.Userx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ProductSubscriptionRepository extends JpaRepository<ProductSubscription, Long> {
    Optional<ProductSubscription> findByProductIdAndUser(Long productId, Userx user);
    Collection<ProductSubscription> findAllByProductIdAndNotifyOnContaining(Long productId, ProductEventType productEventType);
}
