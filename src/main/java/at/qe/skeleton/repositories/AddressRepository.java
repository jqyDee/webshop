package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
