package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingCartItemDTO(
        @NotNull Long productId,
        @NotBlank int quantity
) {}
