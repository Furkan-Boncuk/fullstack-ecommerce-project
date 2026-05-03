ALTER TABLE orders
    ADD COLUMN shipping_first_name VARCHAR(512),
    ADD COLUMN shipping_last_name VARCHAR(512),
    ADD COLUMN shipping_phone_number VARCHAR(512),
    ADD COLUMN shipping_address VARCHAR(512),
    ADD COLUMN shipping_city VARCHAR(512),
    ADD COLUMN shipping_country VARCHAR(512),
    ADD COLUMN shipping_zip_code VARCHAR(512);
