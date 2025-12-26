package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;


public record OrderResponseDTO(
        @NotBlank boolean success,
        Long orderId,
        OrderDTO orderDTO
) {}
