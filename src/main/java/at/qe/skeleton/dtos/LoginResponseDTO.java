package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginResponseDTO(
        @NotBlank String bearerToken
) {}
