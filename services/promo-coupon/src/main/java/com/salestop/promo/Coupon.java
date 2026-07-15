package com.salestop.promo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
class Coupon {
  @Id
  @Column(length = 64)
  private String code;

  @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
  private BigDecimal discountPercent;

  @Column(name = "minimum_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal minimumTotal;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "max_uses", nullable = false)
  private int maxUses;

  @Column(name = "used_count", nullable = false)
  private int usedCount;

  @Column(nullable = false)
  private boolean active;

  protected Coupon() {}

  Coupon(
      String code,
      BigDecimal discountPercent,
      BigDecimal minimumTotal,
      Instant startsAt,
      Instant expiresAt,
      int maxUses) {
    this.code = code.toUpperCase();
    this.discountPercent = discountPercent;
    this.minimumTotal = minimumTotal;
    this.startsAt = startsAt;
    this.expiresAt = expiresAt;
    this.maxUses = maxUses;
    this.active = true;
  }

  String code() {
    return code;
  }

  BigDecimal discount() {
    return discountPercent;
  }

  BigDecimal minimum() {
    return minimumTotal;
  }

  Instant starts() {
    return startsAt;
  }

  Instant expires() {
    return expiresAt;
  }

  int maxUses() {
    return maxUses;
  }

  int used() {
    return usedCount;
  }

  boolean active() {
    return active;
  }
}
