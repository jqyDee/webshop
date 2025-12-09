package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record OrderResponseDTO(
        @NotBlank boolean failed,
        Long orderId,
        OrderDTO order,
        Map<Long, Integer> productsInStock // Long = productId, Integer = available stock
) {}
