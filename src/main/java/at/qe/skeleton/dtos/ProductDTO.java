package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductDTO(
        @NotNull Long id,
        @NotBlank String name,
        @NotNull double price,
        @NotNull int stock,
        @NotNull double discount,
        @NotNull double discountedPrice,
        String shortDescription,
        String description,
        Double rating,
        String imageUrl,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
