package at.qe.skeleton.dtos;

import at.qe.skeleton.model.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record OrderDTO(
        Long id,
        @NotNull UserxDTO userId,
        @NotNull OrderStatus status,
        AddressDTO shippingAddress,
        AddressDTO paymentAddress,
        @NotNull double sum,
        @NotEmpty Set<OrderItemDTO> products,
        LocalDateTime createdDate
) {}
