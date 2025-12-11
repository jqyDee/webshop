package at.qe.skeleton.dtos;


import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public record PageableListDTO<T>(
        Integer pageSize,
        Integer pageIdAfter,
        @NotNull Long totalCount,
        Collection<T> items
) {}
