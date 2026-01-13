package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotNull;

public record CartItemDTO(
        Integer id,
        @NotNull ProductDTO product,
        @NotNull UserxDTO user,
        @NotNull int quantity
) {}
