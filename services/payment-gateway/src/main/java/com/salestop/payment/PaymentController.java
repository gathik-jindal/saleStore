package com.salestop.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController {
  private final PaymentRepository payments;
  private final PaymentService service;
  private final String webhookSecret;

  PaymentController(
      PaymentRepository payments,
      PaymentService service,
      @Value("${payment.webhook-secret:local-webhook-secret}") String secret) {
    this.payments = payments;
    this.service = service;
    this.webhookSecret = secret;
  }

  @GetMapping("/orders/{orderId}")
  PaymentResponse get(@PathVariable UUID orderId) {
    return PaymentResponse.from(
        payments
            .findByOrderId(orderId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")));
  }

  @PostMapping("/webhooks/provider")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void webhook(@RequestHeader("X-Webhook-Secret") String secret, @RequestBody Webhook body) {
    if (!webhookSecret.equals(secret)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    if (!Set.of("SUCCEEDED", "FAILED", "REFUNDED").contains(body.status()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported status");
    service.update(body.orderId(), body.status(), body.providerReference());
  }

  record Webhook(UUID orderId, String status, String providerReference) {}

  record PaymentResponse(
      UUID id,
      UUID orderId,
      UUID userId,
      BigDecimal amount,
      String status,
      String providerReference,
      Instant updatedAt) {
    static PaymentResponse from(Payment p) {
      return new PaymentResponse(
          p.id(), p.orderId(), p.userId(), p.amount(), p.status(), p.reference(), p.updatedAt());
    }
  }
}
