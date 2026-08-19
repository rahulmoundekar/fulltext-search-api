package com.rahul.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String title,
        String description,
        String brand,
        String category,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}