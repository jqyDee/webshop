package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        Long id,
        @NotBlank String street,
        @NotBlank String number,
        @NotBlank String postalCode,
        @NotBlank String city,
        @NotBlank String country
) {}
