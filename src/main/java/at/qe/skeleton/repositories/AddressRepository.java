package at.qe.skeleton.repositories;

import at.qe.skeleton.model.Address;
import at.qe.skeleton.model.Userx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AddressRepository extends JpaRepository<Address, Long> {
        Collection<Address> findAddressByUser(Userx currentUser);
}
