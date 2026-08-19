package com.rahul.api.controller;

import com.rahul.api.dto.FuzzySearchResponse;
import com.rahul.api.dto.ProductRequest;
import com.rahul.api.dto.ProductResponse;
import com.rahul.api.dto.ProductSearchResponse;
import com.rahul.api.enums.SearchSort;
import com.rahul.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {

        return ResponseEntity.ok(productService.getAll());
    }

    @Operation(
            summary = "Search products",
            description = """
                Performs PostgreSQL full-text search with fuzzy-search fallback.

                Supports:
                - Full-text search using PostgreSQL tsvector/tsquery
                - Relevance ranking using ts_rank()
                - Fuzzy matching using pg_trgm
                - Brand and category filtering
                - Price range filtering
                - Sorting
                - Pagination
                """
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ProductSearchResponse>> search(

            @RequestParam @NotBlank(message = "Search query is required") @Size(max = 200, message = "Search query must not exceed 200 characters") String q,

            @RequestParam(required = false) String brand,

            @RequestParam(required = false) String category,

            @RequestParam(required = false) @DecimalMin(value = "0.0", message = "Minimum price must be greater than or equal to zero") BigDecimal minPrice,

            @RequestParam(required = false) @DecimalMin(value = "0.0", message = "Maximum price must be greater than or equal to zero") BigDecimal maxPrice,

            @RequestParam(defaultValue = "RELEVANCE") SearchSort sort,

            @RequestParam(defaultValue = "0") @Min(0) int page,

            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(productService.search(q.trim(), brand, category, minPrice, maxPrice, sort, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> suggestions(@RequestParam("q") String query, @RequestParam(defaultValue = "5") int limit) {

        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(productService.getSuggestions(query, limit));
    }

    @GetMapping("/fuzzy-search")
    public ResponseEntity<List<FuzzySearchResponse>> fuzzySearch(@RequestParam("q") String query, @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(productService.fuzzySearch(query, limit));
    }

}