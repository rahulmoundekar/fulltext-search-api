package com.rahul.api.dto;

import java.math.BigDecimal;

public record ProductSearchResponse(
        Long id,
        String title,
        String description,
        String brand,
        String category,
        BigDecimal price,
        Double textScore,
        Double rating,
        Integer reviewCount,
        Double score,
        String titleHighlight,
        String descriptionHighlight
) {
}
