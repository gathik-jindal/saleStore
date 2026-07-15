package com.salestop.promo;

import org.springframework.data.jpa.repository.JpaRepository;

interface CouponRepository extends JpaRepository<Coupon, String> {}
