package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotNull;

public record CartItemDTO(
        Long id,
        @NotNull ProductDTO product,
        @NotNull UserxDTO user,
        @NotNull int quantity
) {}
