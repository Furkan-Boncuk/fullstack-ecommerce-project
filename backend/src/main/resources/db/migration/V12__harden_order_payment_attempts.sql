ALTER TABLE order_items
    ADD COLUMN product_name VARCHAR(255),
    ADD COLUMN product_image_url VARCHAR(500);

UPDATE order_items oi
SET product_name = p.name,
    product_image_url = p.image_url
FROM products p
WHERE oi.product_id = p.id
  AND oi.product_name IS NULL;

UPDATE order_items
SET product_name = 'Unknown Product'
WHERE product_name IS NULL;

ALTER TABLE order_items
    ALTER COLUMN product_name SET NOT NULL;

ALTER TABLE orders
    ADD COLUMN expires_at TIMESTAMPTZ;

UPDATE orders
SET expires_at = created_at + INTERVAL '30 minutes'
WHERE expires_at IS NULL;

ALTER TABLE orders
    ALTER COLUMN expires_at SET NOT NULL;

ALTER TABLE payments
    ALTER COLUMN checkout_url TYPE VARCHAR(1024),
    ALTER COLUMN checkout_token TYPE VARCHAR(190),
    ADD COLUMN error_code VARCHAR(120);

CREATE TABLE payment_attempts (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    attempt_reference VARCHAR(120) NOT NULL UNIQUE,
    checkout_token VARCHAR(190) UNIQUE,
    checkout_url VARCHAR(1024),
    transaction_id VARCHAR(120),
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    error_code VARCHAR(120),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_attempts_payment_id ON payment_attempts(payment_id);
CREATE INDEX idx_payment_attempts_order_id ON payment_attempts(order_id);
CREATE INDEX idx_payment_attempts_status_expires_at ON payment_attempts(status, expires_at);
CREATE INDEX idx_orders_user_status_created ON orders(user_id, status, created_at DESC);
CREATE INDEX idx_orders_expirable ON orders(expires_at) WHERE status IN ('PENDING', 'PAYMENT_FAILED');
