package at.qe.skeleton.dtos;

import at.qe.skeleton.model.UserxRole;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data transfer object for the UserxTypes Entity.
 * 
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
public record UserxDTO (
    Long id,
    Long createdBy,
    LocalDateTime createDate,
    Long updatedBy,
    LocalDateTime updateDate,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone,
    boolean enabled,
    Set<UserxRole> roles
) {}
