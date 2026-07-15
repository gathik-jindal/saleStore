CREATE TABLE coupons (
 code VARCHAR(64) PRIMARY KEY,
 discount_percent NUMERIC(5,2) NOT NULL CHECK (discount_percent > 0 AND discount_percent <= 100),
 minimum_total NUMERIC(12,2) NOT NULL DEFAULT 0,
 starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
 expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
 max_uses INT NOT NULL CHECK (max_uses > 0),
 used_count INT NOT NULL DEFAULT 0,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
