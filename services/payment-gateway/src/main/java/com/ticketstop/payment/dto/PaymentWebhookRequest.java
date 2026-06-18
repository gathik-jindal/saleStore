package com.ticketstop.payment.dto;

import java.util.UUID;

import com.ticketstop.payment.domain.PaymentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
    @NotNull UUID orderId,
    @NotBlank String providerReference,
    @NotNull PaymentStatus status,
    String failureReason
) {}
