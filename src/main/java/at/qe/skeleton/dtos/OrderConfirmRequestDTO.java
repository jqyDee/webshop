package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotNull;

public record OrderConfirmRequestDTO(
        @NotNull AddressDTO shippingAddress,
        @NotNull AddressDTO paymentAddress
) {}
