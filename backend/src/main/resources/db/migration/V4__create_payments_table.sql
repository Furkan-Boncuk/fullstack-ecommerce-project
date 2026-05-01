CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    provider VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(120),
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE(order_id)
);

