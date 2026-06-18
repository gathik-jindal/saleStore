package com.ticketstop.promo.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ticketstop.promo.dto.CouponRequest;
import com.ticketstop.promo.dto.CouponResponse;
import com.ticketstop.promo.dto.ValidateCouponRequest;
import com.ticketstop.promo.dto.ValidateCouponResponse;
import com.ticketstop.promo.service.PromoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/promos")
public class PromoController {
    private final PromoService promoService;

    public PromoController(PromoService promoService) {
        this.promoService = promoService;
    }

    @GetMapping("/coupons")
    public List<CouponResponse> list() {
        return promoService.list();
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return promoService.create(request);
    }

    @PostMapping("/validate")
    public ValidateCouponResponse validate(@Valid @RequestBody ValidateCouponRequest request) {
        return promoService.validate(request);
    }
}
