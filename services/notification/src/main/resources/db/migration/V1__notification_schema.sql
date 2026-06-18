CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    order_id UUID,
    channel VARCHAR(40) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    subject VARCHAR(255),
    body VARCHAR(2000) NOT NULL,
    status VARCHAR(40) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
