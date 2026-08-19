package com.rahul.api.dto;

import java.math.BigDecimal;

public record FuzzySearchResponse(
        Long id,
        String title,
        String description,
        String brand,
        String category,
        BigDecimal price,
        Double rating,
        Integer reviewCount,
        Double textScore,
        Double score
) {
}