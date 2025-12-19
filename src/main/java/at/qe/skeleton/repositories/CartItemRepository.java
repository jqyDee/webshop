package at.qe.skeleton.repositories;

import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Userx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Collection<CartItem> findAllByUser(Userx currentUser);
    @Modifying
    void deleteAllByUser(Userx currentUser);
}
