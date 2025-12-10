package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.Collection;

public record PageableListDTO<T>(
        int pageSize,
        int pageIdAfter,
        @NotBlank int totalCount,
        Collection<T> items
) {}
