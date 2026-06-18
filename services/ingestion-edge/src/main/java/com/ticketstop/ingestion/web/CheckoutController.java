package com.ticketstop.ingestion.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ticketstop.ingestion.dto.CheckoutRequest;
import com.ticketstop.ingestion.dto.CheckoutResponse;
import com.ticketstop.ingestion.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CheckoutResponse checkout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                     @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(authorization, request);
    }
}
