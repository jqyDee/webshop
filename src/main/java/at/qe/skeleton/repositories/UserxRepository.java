package at.qe.skeleton.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import at.qe.skeleton.model.Userx;
import java.util.Optional;

/**
 * Repository for managing {@link Userx} entities.
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
public interface UserxRepository extends JpaRepository<Userx, Long> {

    Optional<Userx> findFirstByUsername(String username);

    boolean existsByUsername(String username);
}
