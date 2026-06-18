package com.ticketstop.promo.dto;

import java.math.BigDecimal;

public record ValidateCouponResponse(boolean valid, String reason, BigDecimal discountAmount, BigDecimal finalAmount) {}
