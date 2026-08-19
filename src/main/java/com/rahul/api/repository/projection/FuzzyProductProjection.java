package com.rahul.api.repository.projection;

import java.math.BigDecimal;

public interface FuzzyProductProjection {

    Long getId();

    String getTitle();

    String getDescription();

    String getBrand();

    String getCategory();

    BigDecimal getPrice();

    Double getRating();

    Integer getReviewCount();

    Double getTextScore();

    Double getScore();
}
