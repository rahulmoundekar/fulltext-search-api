CREATE INDEX idx_products_title_trgm
    ON products
        USING GIN (LOWER(title) gin_trgm_ops);