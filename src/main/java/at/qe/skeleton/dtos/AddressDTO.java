package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressDTO(
        @NotNull Long userId,
        @NotBlank String street,
        @NotBlank String number,
        @NotBlank String postalCode,
        @NotBlank String city,
        @NotBlank String country
) {}
