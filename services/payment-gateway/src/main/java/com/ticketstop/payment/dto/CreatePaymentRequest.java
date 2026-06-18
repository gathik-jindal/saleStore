package com.ticketstop.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
    @NotNull UUID orderId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank @Size(min = 3, max = 3) String currency,
    @NotBlank String provider
) {}
