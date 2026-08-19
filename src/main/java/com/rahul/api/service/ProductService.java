package com.rahul.api.service;

import com.rahul.api.dto.FuzzySearchResponse;
import com.rahul.api.dto.ProductRequest;
import com.rahul.api.dto.ProductResponse;
import com.rahul.api.dto.ProductSearchResponse;
import com.rahul.api.enums.SearchSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    List<ProductResponse> getAll();

    ProductResponse getById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    Page<ProductSearchResponse> search(
            String query,
            String brand,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            SearchSort sort,
            Pageable pageable
    );

    List<String> getSuggestions(String query, int limit);

    List<FuzzySearchResponse> fuzzySearch(
            String query,
            int limit
    );

}