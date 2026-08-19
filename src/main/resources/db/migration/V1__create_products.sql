CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,

                          title VARCHAR(200) NOT NULL,

                          description TEXT,

                          brand VARCHAR(100),

                          category VARCHAR(100),

                          price NUMERIC(12, 2) NOT NULL,

                          created_at TIMESTAMP NOT NULL,

                          updated_at TIMESTAMP NOT NULL,

                          search_vector TSVECTOR
);


CREATE FUNCTION products_search_vector_update()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(
            to_tsvector('english', COALESCE(NEW.title, '')),
            'A'
        )
        ||
        setweight(
            to_tsvector('english', COALESCE(NEW.description, '')),
            'B'
        )
        ||
        setweight(
            to_tsvector('english', COALESCE(NEW.brand, '')),
            'A'
        )
        ||
        setweight(
            to_tsvector('english', COALESCE(NEW.category, '')),
            'B'
        );

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER products_search_vector_trigger
    BEFORE INSERT OR UPDATE
                         ON products
                         FOR EACH ROW
                         EXECUTE FUNCTION products_search_vector_update();


CREATE INDEX idx_products_search_vector
    ON products
    USING GIN (search_vector);