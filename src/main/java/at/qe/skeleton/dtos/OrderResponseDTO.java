package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record OrderResponseDTO(
        @NotBlank boolean success,
        Long orderId,
        OrderDTO orderDTO
) {}
