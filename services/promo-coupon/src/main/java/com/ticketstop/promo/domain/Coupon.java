package com.ticketstop.promo.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    @Column(name = "redeemed_count", nullable = false)
    private int redeemedCount;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Version
    private long version;

    protected Coupon() {
    }

    public Coupon(UUID id, String code, DiscountType discountType, BigDecimal discountValue, BigDecimal minimumOrderAmount,
                  OffsetDateTime startsAt, OffsetDateTime endsAt, Integer maxRedemptions, boolean active) {
        this.id = id;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderAmount = minimumOrderAmount;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.maxRedemptions = maxRedemptions;
        this.active = active;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }

    public boolean isUsable(BigDecimal orderAmount, OffsetDateTime now) {
        return active
            && orderAmount.compareTo(minimumOrderAmount) >= 0
            && (startsAt == null || !startsAt.isAfter(now))
            && (endsAt == null || endsAt.isAfter(now))
            && (maxRedemptions == null || redeemedCount < maxRedemptions);
    }

    public BigDecimal discountFor(BigDecimal orderAmount) {
        if (discountType == DiscountType.FIXED) {
            return discountValue.min(orderAmount);
        }
        return orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
    }

    public void redeem() {
        redeemedCount++;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public Integer getMaxRedemptions() { return maxRedemptions; }
    public int getRedeemedCount() { return redeemedCount; }
    public boolean isActive() { return active; }
}
