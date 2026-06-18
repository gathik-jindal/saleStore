package com.ticketstop.promo.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.promo.domain.Coupon;
import com.ticketstop.promo.dto.CouponRequest;
import com.ticketstop.promo.dto.CouponResponse;
import com.ticketstop.promo.dto.ValidateCouponRequest;
import com.ticketstop.promo.dto.ValidateCouponResponse;
import com.ticketstop.promo.repository.CouponRepository;

@Service
public class PromoService {
    private final CouponRepository coupons;

    public PromoService(CouponRepository coupons) {
        this.coupons = coupons;
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        String code = normalize(request.code());
        if (coupons.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        Coupon coupon = coupons.save(new Coupon(UUID.randomUUID(), code, request.discountType(), request.discountValue(),
            request.minimumOrderAmount(), request.startsAt(), request.endsAt(), request.maxRedemptions(),
            request.active() == null || request.active()));
        return CouponResponse.from(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> list() {
        return coupons.findAll().stream().map(CouponResponse::from).toList();
    }

    @Transactional
    public ValidateCouponResponse validate(ValidateCouponRequest request) {
        return coupons.findByCodeIgnoreCase(normalize(request.code()))
            .map(coupon -> validateCoupon(coupon, request))
            .orElseGet(() -> invalid("Coupon not found", request.orderAmount()));
    }

    private ValidateCouponResponse validateCoupon(Coupon coupon, ValidateCouponRequest request) {
        if (!coupon.isUsable(request.orderAmount(), OffsetDateTime.now())) {
            return invalid("Coupon is not currently eligible", request.orderAmount());
        }
        if (request.redeem()) {
            coupon.redeem();
        }
        BigDecimal discount = coupon.discountFor(request.orderAmount());
        return new ValidateCouponResponse(true, "ok", discount, request.orderAmount().subtract(discount));
    }

    private ValidateCouponResponse invalid(String reason, BigDecimal orderAmount) {
        return new ValidateCouponResponse(false, reason, BigDecimal.ZERO, orderAmount);
    }

    private String normalize(String code) {
        return code.trim().toUpperCase();
    }
}
