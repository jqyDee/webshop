package at.qe.skeleton.dtos;

import at.qe.skeleton.model.Address;
import jakarta.validation.constraints.NotNull;

public record OrderConfirmRequestDTO(
        @NotNull
        Long orderId,
        @NotNull
        Address shippingAddress,
        @NotNull
        Address paymentAddress
) {}
