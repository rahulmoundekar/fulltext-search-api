package com.rahul.api.service.impl;

import com.rahul.api.config.SearchConstants;
import com.rahul.api.dto.*;
import com.rahul.api.entity.Product;
import com.rahul.api.enums.SearchSort;
import com.rahul.api.exception.ProductNotFoundException;
import com.rahul.api.repository.ProductRepository;
import com.rahul.api.repository.projection.ProductSearchProjection;
import com.rahul.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse create(ProductRequest request) {

        Product product = Product.builder().title(request.title()).description(request.description()).brand(request.brand()).category(request.category()).price(request.price()).build();

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {

        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {

        Product product = productRepository.findById(id).orElseThrow(() ->  new ProductNotFoundException(id));

        return mapToResponse(product);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setCategory(request.category());
        product.setPrice(request.price());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void delete(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSearchResponse> search(String query, String brand, String category, BigDecimal minPrice, BigDecimal maxPrice, SearchSort sort, Pageable pageable) {

        String normalizedQuery = validateQuery(query);

        /* PostgreSQL parsing */
        String parsedQuery = productRepository.parseSearchQuery(normalizedQuery);

        if (parsedQuery == null || parsedQuery.isBlank()) {
            throw new IllegalArgumentException("Search query does not contain searchable terms");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Page<ProductSearchProjection> results = productRepository.searchProducts(normalizedQuery, normalizeFilter(brand), normalizeFilter(category), minPrice, maxPrice, sort.name(), pageable);

        /*
         * NORMAL FULL-TEXT SEARCH
         */
        if (results.hasContent()) {

            return results.map(row -> new ProductSearchResponse(row.getId(), row.getTitle(), row.getDescription(), row.getBrand(), row.getCategory(), row.getPrice(), row.getTextScore(), row.getRating(), row.getReviewCount(), row.getScore(), row.getTitleHighlight(), row.getDescriptionHighlight()));
        }

        /*
         * FUZZY SEARCH FALLBACK
         */
        int offset =
                pageable.getPageNumber()
                        * pageable.getPageSize();

        List<FuzzySearchResponse> fuzzyResults =
                fuzzySearch(
                        normalizedQuery,
                        brand,
                        category,
                        minPrice,
                        maxPrice,
                        pageable.getPageSize(),
                        offset
                );

        long totalFuzzyResults =
                productRepository.countFuzzySearch(
                        normalizedQuery,
                        SearchConstants.FUZZY_SEARCH_THRESHOLD,
                        normalizeFilter(brand),
                        normalizeFilter(category),
                        minPrice,
                        maxPrice
                );

        List<ProductSearchResponse> fuzzyResponses = fuzzyResults.stream().map(row -> new ProductSearchResponse(
                row.id(),
                row.title(),
                row.description(),
                row.brand(),
                row.category(),
                row.price(),
                row.textScore(),
                row.rating(),
                row.reviewCount(),
                row.score(),
                null,
                null
        )).toList();

        return new PageImpl<>(fuzzyResponses, pageable, totalFuzzyResults);
    }

    public List<String> getSuggestions(String query, int limit) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = query.trim();

        int safeLimit = Math.clamp(limit, 1, 10);

        return productRepository.findSuggestions(normalizedQuery, safeLimit);
    }

    public List<FuzzySearchResponse> fuzzySearch(String query, int limit) {

        String normalizedQuery = validateQuery(query);

        int safeLimit = Math.clamp(limit, 1, SearchConstants.MAX_PAGE_SIZE);

        return productRepository.fuzzySearch(normalizedQuery, SearchConstants.FUZZY_SEARCH_THRESHOLD, safeLimit).stream().map(p -> new FuzzySearchResponse(p.getId(), p.getTitle(), p.getDescription(), p.getBrand(), p.getCategory(), p.getPrice(), p.getRating(), p.getReviewCount(), p.getTextScore(), p.getScore())).toList();
    }

    private List<FuzzySearchResponse> fuzzySearch(
            String query,
            String brand,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int limit, int offset
    ) {

        return productRepository
                .fuzzySearch(
                        query,
                        SearchConstants.FUZZY_SEARCH_THRESHOLD,
                        normalizeFilter(brand),
                        normalizeFilter(category),
                        minPrice,
                        maxPrice,
                        limit,
                        offset
                )
                .stream()
                .map(p -> new FuzzySearchResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.getBrand(),
                        p.getCategory(),
                        p.getPrice(),
                        p.getRating(),
                        p.getReviewCount(),
                        p.getTextScore(),
                        p.getScore()
                ))
                .toList();
    }

    private String normalizeFilter(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(product.getId(), product.getTitle(), product.getDescription(), product.getBrand(), product.getCategory(), product.getPrice(), product.getCreatedAt(), product.getUpdatedAt());
    }

    private int normalizePageSize(int size) {

        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        return Math.min(size, SearchConstants.MAX_PAGE_SIZE);
    }

    private String validateQuery(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be empty");
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < SearchConstants.MIN_QUERY_LENGTH) {

            throw new IllegalArgumentException("Search query must contain at least " + SearchConstants.MIN_QUERY_LENGTH + " characters");
        }

        if (normalizedQuery.length() > SearchConstants.MAX_QUERY_LENGTH) {

            throw new IllegalArgumentException("Search query must not exceed " + SearchConstants.MAX_QUERY_LENGTH + " characters");
        }

        return normalizedQuery;
    }
}