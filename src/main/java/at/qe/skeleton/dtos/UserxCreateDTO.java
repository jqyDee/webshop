package at.qe.skeleton.dtos;

import at.qe.skeleton.model.UserxRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Reduced data transfer object for the UserxTypes Entity in the create endpoint.
 *
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
public record UserxCreateDTO(
    @NotBlank
    String username,
    @NotBlank
    String password,
    String firstName,
    String lastName,
    String email,
    String phone,
    boolean enabled,
    @NotEmpty
    Set<UserxRole> roles
) {}
