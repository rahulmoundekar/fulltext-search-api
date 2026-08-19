package com.rahul.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductSearchRequest(

        @NotBlank(message = "Search query must not be blank")
        @Size(
                min = 1,
                max = 200,
                message = "Search query must be between 1 and 200 characters"
        )
        String q,

        @Min(value = 0, message = "Page must be greater than or equal to 0")
        int page,

        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 50, message = "Size must not exceed 50")
        int size
) {
}