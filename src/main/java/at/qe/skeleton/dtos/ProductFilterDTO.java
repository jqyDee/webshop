package at.qe.skeleton.dtos;

public record ProductFilterDTO(
        String name,
        Double minRating,
        Double minPrice,
        Double maxPrice,
        Integer minStock
) {}
