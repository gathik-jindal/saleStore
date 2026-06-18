CREATE TABLE inventory_items (
    product_id UUID PRIMARY KEY,
    available_stock INT NOT NULL,
    reserved_stock INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reservation_records (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    product_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservation_records_product_user ON reservation_records(product_id, user_id);
CREATE INDEX idx_reservation_records_expires_at ON reservation_records(expires_at);
