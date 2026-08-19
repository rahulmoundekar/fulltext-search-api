package com.rahul.api.config;

public final class SearchConstants {

    private SearchConstants() {
    }

    public static final int MIN_QUERY_LENGTH = 2;
    public static final int MAX_QUERY_LENGTH = 100;

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    public static final int DEFAULT_SUGGESTION_LIMIT = 5;
    public static final int MAX_SUGGESTION_LIMIT = 10;

    public static final double FUZZY_SEARCH_THRESHOLD = 0.30;
}