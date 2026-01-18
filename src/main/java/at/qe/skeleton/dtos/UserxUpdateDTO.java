package at.qe.skeleton.dtos;

import at.qe.skeleton.model.UserxRole;
import jakarta.validation.constraints.NotBlank;

/**
 * Reduced data transfer object for the UserxTypes Entity in the create endpoint.
 *
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
public record UserxUpdateDTO(
    // The id is not strictly necessary here but makes the frontend update and delete API calls easier. It is debatable to leave this in here
    Long id,
    @NotBlank String username,
    String password,
    String firstName,
    String lastName,
    String email,
    String phone,
    boolean enabled,
    AddressDTO shippingAddress,
    AddressDTO paymentAddress,
    UserxRole role
) {}
