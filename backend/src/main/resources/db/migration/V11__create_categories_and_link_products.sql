CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(500),
    parent_id BIGINT REFERENCES categories(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_categories_active_sort ON categories(sort_order) WHERE active = TRUE;
CREATE INDEX idx_categories_parent_sort_active ON categories(parent_id, sort_order) WHERE active = TRUE;

INSERT INTO categories (name, slug, description, image_url, sort_order, active)
VALUES
    ('Elektronik', 'elektronik', 'Elektronik urun kategorisi.', 'https://picsum.photos/id/180/900/700', 10, TRUE),
    ('Giyim', 'giyim', 'Giyim urun kategorisi.', 'https://picsum.photos/id/1027/900/700', 20, TRUE),
    ('Ev & Yasam', 'ev-yasam', 'Ev ve yasam urun kategorisi.', 'https://picsum.photos/id/292/900/700', 30, TRUE),
    ('Spor', 'spor', 'Spor urun kategorisi.', 'https://picsum.photos/id/64/900/700', 40, TRUE)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (name, slug, description, sort_order, active)
SELECT 'Genel', 'genel', 'Kategori bilgisi olmayan eski urunler.', 999, TRUE
WHERE EXISTS (
    SELECT 1
    FROM products p
    WHERE p.category IS NULL
       OR NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = p.category)
)
ON CONFLICT (slug) DO NOTHING;

ALTER TABLE products
    ADD COLUMN category_id BIGINT;

UPDATE products p
SET category_id = c.id
FROM categories c
WHERE c.slug = p.category;

UPDATE products p
SET category_id = c.id
FROM categories c
WHERE p.category_id IS NULL
  AND c.slug = 'genel';

ALTER TABLE products
    ALTER COLUMN category_id SET NOT NULL,
    ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id);

DROP INDEX IF EXISTS idx_products_category_active;
CREATE INDEX idx_products_category_active ON products(category_id) WHERE active = TRUE;

ALTER TABLE products
    DROP COLUMN category;
