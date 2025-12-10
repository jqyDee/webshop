package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductDTO(
        @NotBlank Long id,
        @NotNull String name,
        @NotBlank double price,
        @NotBlank int stock,
        double discount,
        String shortDescription,
        String description,
        String imageUrl,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
