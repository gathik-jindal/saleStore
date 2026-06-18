package com.ticketstop.promo.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketstop.promo.domain.Coupon;
import com.ticketstop.promo.domain.DiscountType;

public record CouponResponse(UUID id, String code, DiscountType discountType, BigDecimal discountValue,
                             BigDecimal minimumOrderAmount, OffsetDateTime startsAt, OffsetDateTime endsAt,
                             Integer maxRedemptions, int redeemedCount, boolean active) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getId(), coupon.getCode(), coupon.getDiscountType(), coupon.getDiscountValue(),
            coupon.getMinimumOrderAmount(), coupon.getStartsAt(), coupon.getEndsAt(), coupon.getMaxRedemptions(),
            coupon.getRedeemedCount(), coupon.isActive());
    }
}
