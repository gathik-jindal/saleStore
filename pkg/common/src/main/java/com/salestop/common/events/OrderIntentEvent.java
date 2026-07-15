package com.salestop.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderIntentEvent(
    UUID orderId,
    long productId,
    UUID userId,
    int quantity,
    String idempotencyKey,
    String promoCode,
    BigDecimal unitPrice,
    Instant reservedAt) {}
