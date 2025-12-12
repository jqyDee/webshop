package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing {@link Product} entities.
 */
public interface ProductRepository extends JpaRepository<Product, Long>,
                                           JpaSpecificationExecutor<Product> {
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int decreaseStock(@Param("id") Long productId, @Param("quantity") int quantity);

    @Query("SELECT COUNT(*) FROM Product p WHERE p.id = :id AND p.stock >= :quantity")
    int checkStock(@Param("id") Long productId, @Param("quantity") int quantity);
}
