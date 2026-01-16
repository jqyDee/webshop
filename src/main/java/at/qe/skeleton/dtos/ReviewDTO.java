package at.qe.skeleton.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ReviewDTO(
        Long id,
        ProductDTO product,
        UserxDTO author,
        @Min(1) @Max(5) @NotNull int rating,
        @Size(max = 128) @NotBlank String title,
        @Size(max = 512) @NotBlank String comment,
        LocalDateTime createdDate
) {}
