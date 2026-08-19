package com.rahul.api.repository.projection;

import java.math.BigDecimal;

public interface ProductSearchProjection {

    Long getId();

    String getTitle();

    String getDescription();

    String getBrand();

    String getCategory();

    BigDecimal getPrice();

    Double getTextScore();

    Double getRating();

    Integer getReviewCount();

    Double getScore();

    String getTitleHighlight();

    String getDescriptionHighlight();
}
