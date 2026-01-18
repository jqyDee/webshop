package at.qe.skeleton.dtos;

import at.qe.skeleton.model.NotificationType;
import at.qe.skeleton.model.UserxRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data transfer object for the UserxTypes Entity.
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
public record UserxDTO (
    Long id,
    Long createdBy,
    LocalDateTime createdDate,
    Long updatedBy,
    LocalDateTime updatedDate,
    @NotBlank String username,
    @NotBlank String firstName,
    @NotBlank String lastName,
    // obviously you do not have to update the email and phone every time
    String email,
    String phone,
    AddressDTO shippingAddress,
    AddressDTO paymentAddress,
    @NotNull boolean enabled,
    @NotNull UserxRole role,
    Set<NotificationType> notifyOptions
) {}
