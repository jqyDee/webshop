package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotNull;

public record OrderItemDTO (
    Long id,
    @NotNull ProductDTO product,
    @NotNull OrderDTO order,
    String name,
    Integer total,
    Integer quantity
) {}
