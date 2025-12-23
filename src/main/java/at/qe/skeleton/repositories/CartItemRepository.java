package at.qe.skeleton.repositories;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Userx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Collection<CartItem> findAllByUser(Userx currentUser);
    Optional<CartItem> findFirstByUserAndProduct_Id(Userx currentUser, Long product);
    @Modifying
    void deleteAllByUser(Userx currentUser);
}
