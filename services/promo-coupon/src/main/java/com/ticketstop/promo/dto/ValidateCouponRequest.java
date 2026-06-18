package com.ticketstop.promo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateCouponRequest(
    @NotBlank String code,
    @NotNull @DecimalMin("0.00") BigDecimal orderAmount,
    boolean redeem
) {}
