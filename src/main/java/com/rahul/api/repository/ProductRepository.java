package com.rahul.api.repository;

import com.rahul.api.entity.Product;
import com.rahul.api.repository.projection.FuzzyProductProjection;
import com.rahul.api.repository.projection.ProductSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            
                SELECT
                    p.id AS id,
                    p.title AS title,
                    p.description AS description,
                    p.brand AS brand,
                    p.category AS category,
                    p.price AS price,
            
                    ts_rank(
                      p.search_vector,
                      websearch_to_tsquery('english', :query)
                  ) AS textScore,
            
                  p.rating AS rating,
                  p.review_count AS reviewCount,
            
                  (
                      0.70 * ts_rank(
                          p.search_vector,
                          websearch_to_tsquery('english', :query)
                      )
                      +
                      0.20 * COALESCE(p.rating, 0) / 5.0
                      +
                      0.10 * LEAST(
                          LN(1 + COALESCE(p.review_count, 0)) / 10.0,
                          1.0
                      )
                  ) AS score,
            
                    ts_headline(
                        'english',
                        COALESCE(p.title, ''),
                        websearch_to_tsquery('english', :query),
                        'StartSel=<mark>, StopSel=</mark>'
                    ) AS title_highlight,
            
                    ts_headline(
                        'english',
                        COALESCE(p.description, ''),
                        websearch_to_tsquery('english', :query),
                        'StartSel=<mark>, StopSel=</mark>, MaxFragments=2'
                    ) AS description_highlight
            
            FROM products p
            
            WHERE p.search_vector @@
                  websearch_to_tsquery('english', :query)
            
              AND (
                  :brand IS NULL
                  OR LOWER(p.brand) = LOWER(:brand)
              )
            
              AND (
                  :category IS NULL
                  OR LOWER(p.category) = LOWER(:category)
              )
            
              AND (
                  :minPrice IS NULL
                  OR p.price >= :minPrice
              )
            
              AND (
                  :maxPrice IS NULL
                  OR p.price <= :maxPrice
              )
            
            ORDER BY
            
                CASE
                    WHEN :sort = 'RELEVANCE'
                    THEN
                        (
                            ts_rank(
                                p.search_vector,
                                websearch_to_tsquery('english', :query)
                            ) * 0.70
            
                            +
            
                            (p.rating / 5.0) * 0.20
            
                            +
            
                            LEAST(
                                LN(1 + p.review_count) / 10.0,
                                1.0
                            ) * 0.10
                        )
                END DESC,
            
                CASE
                    WHEN :sort = 'PRICE_ASC'
                    THEN p.price
                END ASC,
            
                CASE
                    WHEN :sort = 'PRICE_DESC'
                    THEN p.price
                END DESC,
            
                CASE
                    WHEN :sort = 'NEWEST'
                    THEN p.created_at
                END DESC,
            
                p.id DESC
            """,

            countQuery = """
                    SELECT COUNT(*)
                    FROM products p
                    
                    WHERE p.search_vector @@
                          websearch_to_tsquery('english', :query)
                    
                      AND (
                          :brand IS NULL
                          OR LOWER(p.brand) = LOWER(:brand)
                      )
                    
                      AND (
                          :category IS NULL
                          OR LOWER(p.category) = LOWER(:category)
                      )
                    
                      AND (
                          :minPrice IS NULL
                          OR p.price >= :minPrice
                      )
                    
                      AND (
                          :maxPrice IS NULL
                          OR p.price <= :maxPrice
                      )
                    """,

            nativeQuery = true)
    Page<ProductSearchProjection> searchProducts(@Param("query") String query, @Param("brand") String brand, @Param("category") String category, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, @Param("sort") String sort, Pageable pageable);


    @Query(value = """
            SELECT websearch_to_tsquery(
                'english',
                :query
            )::text
            """, nativeQuery = true)
    String parseSearchQuery(@Param("query") String query);


    @Query(value = """
            SELECT
                p.title
            FROM products p
            WHERE LOWER(p.title)
                  LIKE CONCAT('%', LOWER(:query), '%')
            ORDER BY
                similarity(
                    LOWER(p.title),
                    LOWER(:query)
                ) DESC,
                p.review_count DESC,
                p.title
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findSuggestions(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
            SELECT
                p.id AS id,
                p.title AS title,
                p.description AS description,
                p.brand AS brand,
                p.category AS category,
                p.price AS price,
            
                word_similarity(
                    LOWER(:query),
                    LOWER(p.title)
                ) AS textScore,
            
                p.rating AS rating,
                p.review_count AS reviewCount,
            
                (
                    0.70 * word_similarity(
                        LOWER(:query),
                        LOWER(p.title)
                    )
                    +
                    0.20 * COALESCE(p.rating, 0) / 5.0
                    +
                    0.10 * LEAST(
                        LN(1 + COALESCE(p.review_count, 0)) / 10.0,
                        1.0
                    )
                ) AS score
            
            FROM products p
            
            WHERE word_similarity(
                    LOWER(:query),
                    LOWER(p.title)
                  ) >= :threshold
            
            ORDER BY
                score DESC,
                p.review_count DESC,
                p.title
            
            LIMIT :limit
            """, nativeQuery = true)
    List<FuzzyProductProjection> fuzzySearch(@Param("query") String query, @Param("threshold") double threshold, @Param("limit") int limit);

    @Query(value = """
        SELECT
            p.id AS id,
            p.title AS title,
            p.description AS description,
            p.brand AS brand,
            p.category AS category,
            p.price AS price,

            word_similarity(
                LOWER(:query),
                LOWER(p.title)
            ) AS textScore,

            p.rating AS rating,
            p.review_count AS reviewCount,

            (
                0.70 * word_similarity(
                    LOWER(:query),
                    LOWER(p.title)
                )
                +
                0.20 * COALESCE(p.rating, 0) / 5.0
                +
                0.10 * LEAST(
                    LN(1 + COALESCE(p.review_count, 0)) / 10.0,
                    1.0
                )
            ) AS score

        FROM products p

        WHERE word_similarity(
                LOWER(:query),
                LOWER(p.title)
              ) >= :threshold

          AND (
                :brand IS NULL
                OR LOWER(p.brand) = LOWER(:brand)
              )

          AND (
                :category IS NULL
                OR LOWER(p.category) = LOWER(:category)
              )

          AND (
                :minPrice IS NULL
                OR p.price >= :minPrice
              )

          AND (
                :maxPrice IS NULL
                OR p.price <= :maxPrice
              )

        ORDER BY
            score DESC,
            p.review_count DESC,
            p.title

        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    List<FuzzyProductProjection> fuzzySearch(
            @Param("query") String query,
            @Param("threshold") double threshold,
            @Param("brand") String brand,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM products p

        WHERE word_similarity(
                LOWER(:query),
                LOWER(p.title)
              ) >= :threshold

          AND (
                :brand IS NULL
                OR LOWER(p.brand) = LOWER(:brand)
              )

          AND (
                :category IS NULL
                OR LOWER(p.category) = LOWER(:category)
              )

          AND (
                :minPrice IS NULL
                OR p.price >= :minPrice
              )

          AND (
                :maxPrice IS NULL
                OR p.price <= :maxPrice
              )
        """, nativeQuery = true)
    long countFuzzySearch(
            @Param("query") String query,
            @Param("threshold") double threshold,
            @Param("brand") String brand,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}