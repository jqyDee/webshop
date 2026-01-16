package at.qe.skeleton.services;

import at.qe.skeleton.exceptions.UsernameDuplicateException;
import at.qe.skeleton.model.Userx;
import java.util.Collection;
import at.qe.skeleton.model.UserxRole;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import at.qe.skeleton.repositories.UserxRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for accessing and manipulating user data.
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@Service
public class UserxService implements UserDetailsService {
 
    private final UserxRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public UserxService(UserxRepository userRepository, PasswordEncoder passwordEncoder, AuthenticatedUserService authenticatedUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUserService = authenticatedUserService;
    }
    
    /**
     * Returns a collection of all users.
     *
     * @return the userx collection
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Userx> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns a collection of all managers.
     *
     * @return the userx collection
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Userx> getAllManagers() {
        return userRepository.findByRole(UserxRole.MANAGER);
    }

    /**
     * Loads a single user identified by its id.
     *
     * @param id the id to search for
     * @return the user with the id
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Optional<Userx> loadUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }

        return userRepository.findById(id);
    }
    
    /**
     * Saves the user. This method will also set createdDate for new
     * entities or updatedDate for updated entities. The user
     * requesting this operation will also be stored as createdDate
     * or updateUser respectively.
     *
     * @param user the user to save
     * @return the updated user
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Userx saveUser(Userx user, String newRawPassword) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User or username is null");
        }

        if (user.isNew()) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new UsernameDuplicateException(
                        "Username " + user.getUsername() + " not available");
            }
            user.setPassword(passwordEncoder.encode(newRawPassword));
            user.setCreateUser(authenticatedUserService.getAuthenticatedUser());
        } else {
            if (newRawPassword != null && !newRawPassword.isEmpty()) {
                user.setPassword(passwordEncoder.encode(newRawPassword));
            }

            user.setUpdateUser(authenticatedUserService.getAuthenticatedUser());
        }

        return userRepository.save(user);
    }

    /**
     * Deletes the user.
     *
     * @param user the user to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Userx user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User or User.id is null");
        }

        Userx userFound = userRepository.findById(user.getId())
                                        .orElseThrow(EntityNotFoundException::new);
        userRepository.delete(userFound);
    }

    /**
     * Get user by username
     *
     * @param username username of user
     * @return user
     */
    public Userx getUserByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is null");
        }

        return userRepository.findFirstByUsername(username)
                             .orElseThrow(EntityNotFoundException::new);
    }


    /**
     * Loads a user by its username. Required for JWT authentication.
     *
     * @param username the username identifying the user whose data is required.
     * @return the user with the given username and their details.
     * @throws UsernameNotFoundException if username not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Create a new user
     *
     * @param user new user to add
     * @return added user
     */
    public Userx createUser(Userx user) {
        if (user == null) throw new IllegalArgumentException("User is null");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserxRole.CUSTOMER);
        return userRepository.save(user);
    }

}
