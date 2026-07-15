package com.salestop.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId, long productId, UUID userId, int quantity, BigDecimal total, Instant createdAt) {}
