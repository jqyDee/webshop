package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductDTO(
        @NotNull Long id,
        @NotBlank String name,
        double price,
        int stock,
        double discount,
        String shortDescription,
        String description,
        Double rating,
        String imageUrl,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
