package at.qe.skeleton.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReviewDTO(
        @NotNull Long id,
        @NotNull Long productId,
        @NotBlank int rating,
        @NotNull String title,
        @NotNull String comment,
        @NotNull LocalDateTime createdDate
) {}
