CREATE INDEX idx_products_title_lower_pattern
    ON products (LOWER(title) text_pattern_ops);