package com.ticketstop.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderIntentRequest(
    @NotNull UUID productId,
    @NotBlank String userId,
    UUID reservationId,
    @NotBlank String idempotencyKey,
    @Min(1) int quantity,
    @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
    @NotNull @DecimalMin("0.00") BigDecimal discountAmount
) {}
