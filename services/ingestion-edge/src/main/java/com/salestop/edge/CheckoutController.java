package com.salestop.edge;

import com.salestop.common.events.EventTopics;
import com.salestop.common.events.OrderIntentEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
class CheckoutController {
  private final RateLimiter limiter;
  private final JwtVerifier jwt;
  private final DownstreamClients clients;
  private final KafkaTemplate<String, Object> kafka;

  CheckoutController(
      RateLimiter limiter,
      JwtVerifier jwt,
      DownstreamClients clients,
      KafkaTemplate<String, Object> kafka) {
    this.limiter = limiter;
    this.jwt = jwt;
    this.clients = clients;
    this.kafka = kafka;
  }

  @PostMapping("/checkout")
  Mono<ResponseEntity<CheckoutResponse>> checkout(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody CheckoutRequest request,
      ServerWebExchange exchange) {
    String clientIp =
        exchange.getRequest().getRemoteAddress() == null
            ? "unknown"
            : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    return limiter
        .allow(clientIp)
        .onErrorMap(error -> new Unavailable("Rate limiter is unavailable"))
        .flatMap(
            decision -> {
              if (!decision.allowed()) return Mono.error(new TooManyRequests());
              return Mono.fromCallable(() -> jwt.verify(bearer(authorization)))
                  .subscribeOn(Schedulers.boundedElastic())
                  .flatMap(userId -> process(userId, idempotencyKey, request));
            });
  }

  private Mono<ResponseEntity<CheckoutResponse>> process(
      UUID userId, String idempotencyKey, CheckoutRequest request) {
    if (idempotencyKey.isBlank() || idempotencyKey.length() > 255)
      return Mono.error(new BadRequest("Idempotency-Key must contain 1-255 characters"));
    return clients
        .product(request.productId())
        .flatMap(
            product -> {
              if (!product.active()) return Mono.error(new BadRequest("Product is not active"));
              BigDecimal subtotal =
                  product.price().multiply(BigDecimal.valueOf(request.quantity()));
              return clients
                  .validatePromo(request.promoCode(), subtotal)
                  .flatMap(
                      promo -> {
                        if (!promo.valid()) return Mono.error(new BadRequest(promo.reason()));
                        UUID proposed = UUID.randomUUID();
                        DownstreamClients.Reserve reserve =
                            new DownstreamClients.Reserve(
                                request.productId(),
                                userId,
                                request.quantity(),
                                idempotencyKey,
                                proposed);
                        return clients
                            .reserve(reserve)
                            .flatMap(
                                result -> {
                                  if ("SOLD_OUT".equals(result.status()))
                                    return Mono.error(new Conflict("Product is sold out"));
                                  if ("ALREADY_RESERVED".equals(result.status()))
                                    return Mono.error(
                                        new Conflict("User already reserved this product"));
                                  if ("DUPLICATE".equals(result.status()))
                                    return Mono.just(
                                        ResponseEntity.accepted()
                                            .body(
                                                new CheckoutResponse(
                                                    result.orderId(),
                                                    "ALREADY_ACCEPTED",
                                                    result.remainingStock())));
                                  OrderIntentEvent event =
                                      new OrderIntentEvent(
                                          result.orderId(),
                                          request.productId(),
                                          userId,
                                          request.quantity(),
                                          idempotencyKey,
                                          request.promoCode(),
                                          promo
                                              .finalTotal()
                                              .divide(BigDecimal.valueOf(request.quantity())),
                                          Instant.now());
                                  return Mono.fromFuture(
                                          kafka.send(
                                              EventTopics.ORDER_INTENTS,
                                              result.orderId().toString(),
                                              event))
                                      .map(
                                          ignored ->
                                              ResponseEntity.accepted()
                                                  .body(
                                                      new CheckoutResponse(
                                                          result.orderId(),
                                                          "ACCEPTED",
                                                          result.remainingStock())))
                                      .onErrorResume(
                                          error ->
                                              clients
                                                  .release(
                                                      new DownstreamClients.Release(
                                                          request.productId(),
                                                          userId,
                                                          request.quantity(),
                                                          idempotencyKey,
                                                          result.orderId()))
                                                  .then(
                                                      Mono.error(
                                                          new Unavailable(
                                                              "Could not durably queue order intent"))));
                                });
                      });
            })
        .onErrorMap(
            org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
                .class,
            ex -> new BadRequest("Product not found"))
        .onErrorMap(
            org.springframework.web.reactive.function.client.WebClientException.class,
            ex -> new Unavailable("A checkout dependency is unavailable"))
        .onErrorMap(
            io.grpc.StatusRuntimeException.class,
            ex -> new Unavailable("Inventory is unavailable"));
  }

  private String bearer(String value) {
    if (value == null || !value.startsWith("Bearer "))
      throw new JwtVerifier.Unauthorized("Bearer token required");
    return value.substring(7);
  }

  record CheckoutRequest(
      @Positive long productId, @Min(1) @Max(10) int quantity, @Size(max = 64) String promoCode) {}

  record CheckoutResponse(UUID orderId, String status, int remainingStock) {}

  static class BadRequest extends RuntimeException {
    BadRequest(String m) {
      super(m);
    }
  }

  static class Conflict extends RuntimeException {
    Conflict(String m) {
      super(m);
    }
  }

  static class Unavailable extends RuntimeException {
    Unavailable(String m) {
      super(m);
    }
  }

  static class TooManyRequests extends RuntimeException {}
}
