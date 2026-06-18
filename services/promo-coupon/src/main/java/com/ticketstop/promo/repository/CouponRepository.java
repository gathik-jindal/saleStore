package com.ticketstop.promo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketstop.promo.domain.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
