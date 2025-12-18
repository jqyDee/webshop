package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.dtos.UserxUpdateDTO;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.mappers.UserxUpdateMapper;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.UserxService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * REST controllers for admin users.
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserxUpdateMapper userUpdateMapper;
    private final UserxMapper userMapper;
    private final UserxService userService;

    @Autowired
    public AdminController(UserxMapper userMapper, UserxService userService, UserxUpdateMapper userCreateMapper) {
        this.userUpdateMapper = userCreateMapper;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * GET all existing Managers
     *
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of all existing managers in the body
     */
    @GetMapping("/managers")
    public ResponseEntity<Collection<UserxDTO>> getAllManagers() {
        Collection<Userx> allManagers = userService.getAllManagers();
        List<UserxDTO> allManagersMapped = allManagers.stream().map(userMapper::mapTo).toList();
        return ResponseEntity.ok(allManagersMapped);
    }

    /**
     * GET all existing Users
     *
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of all existing users in the body
     */
    @GetMapping("/users")
    public ResponseEntity<Collection<UserxDTO>> getAllUsers() {
        Collection<Userx> allUsers = userService.getAllUsers();
        List<UserxDTO> allUsersMapped = allUsers.stream().map(userMapper::mapTo).toList();
        return ResponseEntity.ok(allUsersMapped);
    }

    /**
     * GET one User
     *
     * @param id the id to search for
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with the user of given id in the body, or with status {@code 404} if no such user exists
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<UserxDTO> getUser(@PathVariable Long id) {
        Optional<Userx> existingUserx = userService.loadUser(id);
        if (existingUserx.isPresent()) {
            return ResponseEntity.ok(userMapper.mapTo(existingUserx.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a user if the username is not yet used.
     *
     * @param userxUpdateDto the user tb created
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the newly created user in the body, or with status {@code 409 (Conflict)} if the username is already in use
     */
    @PostMapping("/createUser")
    public ResponseEntity<UserxDTO> createUser(@Valid @RequestBody UserxUpdateDTO userxUpdateDto) {
        Userx user = userService.saveUser(userUpdateMapper.mapFrom(userxUpdateDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.mapTo(user));
    }
    
    /**
     * Partially updates user of given id.
     * The update is partial because only a select subset of user fields can be modified after create.
     * 
     * @param id the id of the user tb updated
     * @param userxUpdateDto the updated user information
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the updated user in the body, or with status {@code 404 (Not Found)} if no user with this id exists
     */
    @PatchMapping("/user/{id}")
    public ResponseEntity<UserxDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserxUpdateDTO userxUpdateDto) {
        Optional<Userx> existingUserx = userService.loadUser(id);
        if (existingUserx.isPresent()) {
            Userx user = userUpdateMapper.mapFrom(userxUpdateDto, id);
            Userx savedUser = userService.saveUser(user);
            return ResponseEntity.ok(userMapper.mapTo(savedUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deletes user of given id.
     *
     * @param id the id of the user tb deleted
     * @return {@link ResponseEntity} with status {@code 204 (No Content)} on successful delete, or with status {@code 404 (Not Found)} if no user with this id exists
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<Userx> existingUserx = userService.loadUser(id);
        if (existingUserx.isPresent()) {
            userService.deleteUser(existingUserx.get());
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @DeleteMapping("/product/{productId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long productId, @PathVariable Long reviewId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
