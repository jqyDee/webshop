package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ReviewDTO(
        Long id,
        ProductDTO product,
        UserxDTO author,
        int rating,
        @NotBlank String title,
        @NotBlank String comment,
        LocalDateTime createdAt
) {}
