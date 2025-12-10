package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressDTO(
        @NotNull String street,
        @NotBlank int number,
        @NotNull String postalCode,
        @NotNull String city,
        @NotNull String country
) {}
