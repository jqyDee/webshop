package at.qe.skeleton.dtos;

import at.qe.skeleton.model.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        @NotNull Long id,
        @NotNull UserxDTO user,
        @NotNull OrderStatus status,
        AddressDTO shippingAddress,
        AddressDTO paymentAddress,
        @NotNull double sum,
        @NotEmpty List<OrderItemDTO> products,
        @NotNull LocalDateTime createdDate
) {}
