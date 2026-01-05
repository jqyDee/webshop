package at.qe.skeleton.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReviewDTO(
        Long id,
        ProductDTO product,
        UserxDTO author,
        @Min(1) @Max(5) int rating,
        @Size(max = 128) @NotBlank String title,
        @Size(max = 512) @NotBlank String comment,
        LocalDateTime createdDate
) {}
