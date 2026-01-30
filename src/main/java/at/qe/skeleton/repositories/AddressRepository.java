package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing {@link Address} entities.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
}
