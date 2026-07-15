CREATE TABLE orders (
 id UUID PRIMARY KEY, product_id BIGINT NOT NULL, user_id UUID NOT NULL, quantity INT NOT NULL CHECK(quantity>0),
 idempotency_key VARCHAR(255) NOT NULL UNIQUE, status VARCHAR(50) NOT NULL,
 total NUMERIC(14,2) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_orders_product_user ON orders(product_id,user_id);
CREATE INDEX idx_orders_user_created ON orders(user_id,created_at DESC);
CREATE TABLE outbox_events (
 id UUID PRIMARY KEY, topic VARCHAR(255) NOT NULL, event_key VARCHAR(255) NOT NULL,
 payload TEXT NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, published_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(published_at,created_at);
