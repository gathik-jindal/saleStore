CREATE TABLE orders (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    reservation_id UUID,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_product_user ON orders(product_id, user_id);
CREATE INDEX idx_orders_status ON orders(status);
