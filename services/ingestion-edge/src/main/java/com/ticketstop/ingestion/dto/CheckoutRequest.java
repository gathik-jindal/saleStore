package com.ticketstop.ingestion.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    @NotNull UUID productId,
    @Min(1) int quantity,
    @NotBlank String idempotencyKey,
    @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
    String promoCode
) {}
