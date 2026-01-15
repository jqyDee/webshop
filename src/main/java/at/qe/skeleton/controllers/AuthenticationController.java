/**
 * Spring configuration for web security.
 * <p>
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */

package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.LoginRequestDTO;
import at.qe.skeleton.dtos.LoginResponseDTO;
import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.dtos.UserxUpdateDTO;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.mappers.UserxUpdateMapper;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.AuthenticationService;
import at.qe.skeleton.services.UserxService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserxService userxService;
    private final UserxUpdateMapper userxUpdateMapper;
    private final UserxMapper userxMapper;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    UserxService userxService, UserxUpdateMapper userxUpdateMapper,
                                    UserxMapper userxMapper) {
        this.authenticationService = authenticationService;
        this.userxService = userxService;
        this.userxUpdateMapper = userxUpdateMapper;
        this.userxMapper = userxMapper;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequest the login request containing the username and password
     * @return the JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationService.authenticateLoginRequest(loginRequest.username(), loginRequest.password());
        String token = authenticationService.generateToken(authentication);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    /**
     * Creates a user if the username is not yet used.
     *
     * @param userxUpdateDto the user tb created
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the newly created user in the body, or with status {@code 409 (Conflict)} if the username is already in use
     */
    @PostMapping("/createUser")
    public ResponseEntity<UserxDTO> createUser(@Valid @RequestBody UserxUpdateDTO userxUpdateDto) {
        Userx toCreate = userxUpdateMapper.mapFrom(userxUpdateDto);
        Userx createdUser = userxService.createUser(toCreate);

        return ResponseEntity.status(HttpStatus.CREATED).body(userxMapper.mapTo(createdUser));
    }
}
