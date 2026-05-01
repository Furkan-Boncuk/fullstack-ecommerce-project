ALTER TABLE products
    ADD COLUMN description VARCHAR(1000),
    ADD COLUMN category VARCHAR(120),
    ADD COLUMN image_url VARCHAR(500);

CREATE INDEX idx_products_category_active ON products(category) WHERE active = TRUE;
CREATE INDEX idx_products_stock_active ON products(stock) WHERE active = TRUE;

