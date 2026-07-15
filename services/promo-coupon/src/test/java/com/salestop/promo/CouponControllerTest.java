package com.salestop.promo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CouponControllerTest {
  @Test
  void validatesAnEligibleCouponAndCalculatesTheDiscount() {
    CouponRepository repository = mock(CouponRepository.class);
    Coupon coupon =
        new Coupon(
            "FLASH20",
            BigDecimal.valueOf(20),
            BigDecimal.valueOf(100),
            Instant.now().minusSeconds(60),
            Instant.now().plusSeconds(3600),
            100);
    when(repository.findById("FLASH20")).thenReturn(Optional.of(coupon));
    var result =
        new CouponController(repository, "admin")
            .validate(new CouponController.ValidationRequest("flash20", BigDecimal.valueOf(250)));
    assertThat(result.valid()).isTrue();
    assertThat(result.discountAmount()).isEqualByComparingTo("50.00");
    assertThat(result.finalTotal()).isEqualByComparingTo("200.00");
  }

  @Test
  void rejectsAnExpiredCoupon() {
    CouponRepository repository = mock(CouponRepository.class);
    Coupon coupon =
        new Coupon(
            "OLD",
            BigDecimal.TEN,
            BigDecimal.ZERO,
            Instant.now().minusSeconds(120),
            Instant.now().minusSeconds(60),
            10);
    when(repository.findById("OLD")).thenReturn(Optional.of(coupon));
    assertThat(
            new CouponController(repository, "admin")
                .validate(new CouponController.ValidationRequest("OLD", BigDecimal.TEN))
                .valid())
        .isFalse();
  }
}
