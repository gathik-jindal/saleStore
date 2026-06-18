package com.ticketstop.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketstop.payment.domain.Payment;
import com.ticketstop.payment.domain.PaymentStatus;

public record PaymentResponse(UUID id, UUID orderId, String provider, String providerReference, BigDecimal amount,
                              String currency, PaymentStatus status, String failureReason, OffsetDateTime createdAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getProvider(),
            payment.getProviderReference(), payment.getAmount(), payment.getCurrency(), payment.getStatus(),
            payment.getFailureReason(), payment.getCreatedAt());
    }
}
