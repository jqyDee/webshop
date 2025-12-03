package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.UserxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Userx endpoints exposed by the server.
 *
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@RestController
@RequestMapping("/api/users")
public class UserxController {
 
    private final UserxMapper userMapper;
    private final UserxService userService;

    @Autowired
    public UserxController(UserxMapper userMapper, UserxService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserxDTO> getCurrentUser(@AuthenticationPrincipal Userx user) {
        return ResponseEntity.ok(userMapper.mapTo(user));
    }
     
    @GetMapping("/authenticated")
    public ResponseEntity<String> isAuthenticated(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        return ResponseEntity.ok("User is authenticated: " + userDetails.getUsername());
    }
}
