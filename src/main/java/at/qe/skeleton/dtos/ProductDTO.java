package at.qe.skeleton.dtos;

import at.qe.skeleton.model.ProductEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ProductDTO(
        Long id,
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
        LocalDateTime updatedDate,
        Map<ProductEventType, Boolean> subscriptions
) {}
