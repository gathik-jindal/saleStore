package com.ticketstop.promo.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.ticketstop.promo.domain.DiscountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CouponRequest(
    @NotBlank @Size(max = 80) String code,
    @NotNull DiscountType discountType,
    @NotNull @DecimalMin("0.01") BigDecimal discountValue,
    @NotNull @DecimalMin("0.00") BigDecimal minimumOrderAmount,
    OffsetDateTime startsAt,
    OffsetDateTime endsAt,
    @Min(1) Integer maxRedemptions,
    Boolean active
) {}
