package com.salestop.promo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController {
  private final CouponRepository coupons;
  private final String adminKey;

  CouponController(
      CouponRepository coupons, @Value("${salestop.admin-key:local-admin-key}") String adminKey) {
    this.coupons = coupons;
    this.adminKey = adminKey;
  }

  @PostMapping("/validate")
  ValidationResponse validate(@Valid @RequestBody ValidationRequest request) {
    Coupon coupon = coupons.findById(request.code().toUpperCase()).orElse(null);
    Instant now = Instant.now();
    if (coupon == null)
      return ValidationResponse.invalid("Coupon does not exist", request.subtotal());
    if (!coupon.active() || now.isBefore(coupon.starts()) || !now.isBefore(coupon.expires()))
      return ValidationResponse.invalid("Coupon is not active", request.subtotal());
    if (coupon.used() >= coupon.maxUses())
      return ValidationResponse.invalid("Coupon usage limit reached", request.subtotal());
    if (request.subtotal().compareTo(coupon.minimum()) < 0)
      return ValidationResponse.invalid("Minimum order total not met", request.subtotal());
    BigDecimal discount =
        request
            .subtotal()
            .multiply(coupon.discount())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    return new ValidationResponse(
        true, "Coupon applied", coupon.discount(), discount, request.subtotal().subtract(discount));
  }

  @PostMapping
  ResponseEntity<CouponResponse> create(
      @RequestHeader("X-Admin-Key") String key, @Valid @RequestBody CreateCoupon request) {
    authorize(key);
    if (!request.expiresAt().isAfter(request.startsAt()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expiresAt must be after startsAt");
    if (coupons.existsById(request.code().toUpperCase()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Coupon already exists");
    Coupon c =
        coupons.save(
            new Coupon(
                request.code(),
                request.discountPercent(),
                request.minimumTotal(),
                request.startsAt(),
                request.expiresAt(),
                request.maxUses()));
    return ResponseEntity.status(HttpStatus.CREATED).body(CouponResponse.from(c));
  }

  private void authorize(String key) {
    if (!adminKey.equals(key))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin key");
  }

  record ValidationRequest(@NotBlank String code, @DecimalMin("0.00") BigDecimal subtotal) {}

  record ValidationResponse(
      boolean valid,
      String reason,
      BigDecimal discountPercent,
      BigDecimal discountAmount,
      BigDecimal finalTotal) {
    static ValidationResponse invalid(String reason, BigDecimal total) {
      return new ValidationResponse(false, reason, BigDecimal.ZERO, BigDecimal.ZERO, total);
    }
  }

  record CreateCoupon(
      @NotBlank @Size(max = 64) String code,
      @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal discountPercent,
      @DecimalMin("0.00") BigDecimal minimumTotal,
      @NotNull Instant startsAt,
      @NotNull Instant expiresAt,
      @Min(1) int maxUses) {}

  record CouponResponse(
      String code,
      BigDecimal discountPercent,
      BigDecimal minimumTotal,
      Instant startsAt,
      Instant expiresAt,
      int maxUses,
      int usedCount,
      boolean active) {
    static CouponResponse from(Coupon c) {
      return new CouponResponse(
          c.code(),
          c.discount(),
          c.minimum(),
          c.starts(),
          c.expires(),
          c.maxUses(),
          c.used(),
          c.active());
    }
  }
}
