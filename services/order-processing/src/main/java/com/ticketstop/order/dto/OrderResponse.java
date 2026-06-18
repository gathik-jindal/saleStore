package com.ticketstop.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketstop.order.domain.OrderLedger;
import com.ticketstop.order.domain.OrderStatus;

public record OrderResponse(UUID id, UUID productId, String userId, UUID reservationId, String idempotencyKey,
                            int quantity, BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal totalAmount,
                            OrderStatus status, OffsetDateTime createdAt) {
    public static OrderResponse from(OrderLedger order) {
        return new OrderResponse(order.getId(), order.getProductId(), order.getUserId(), order.getReservationId(),
            order.getIdempotencyKey(), order.getQuantity(), order.getUnitPrice(), order.getDiscountAmount(),
            order.getTotalAmount(), order.getStatus(), order.getCreatedAt());
    }
}
