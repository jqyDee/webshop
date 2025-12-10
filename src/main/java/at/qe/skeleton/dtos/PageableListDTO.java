package at.qe.skeleton.dtos;


import java.util.Collection;

public record PageableListDTO<T>(
        Integer pageSize,
        Integer pageIdAfter,
        Integer totalCount,
        Collection<T> items
) {}
