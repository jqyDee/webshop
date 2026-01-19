package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotNull;

public record OrderItemDTO (
    @NotNull Long id,
    @NotNull ProductDTO product,
    @NotNull String name,
    @NotNull Double total,
    @NotNull Integer quantity
) {}
