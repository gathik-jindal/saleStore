package com.ticketstop.ingestion.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutResponse(boolean accepted, String reason, UUID reservationId, UUID orderId,
                               BigDecimal discountAmount, BigDecimal totalAmount) {}
