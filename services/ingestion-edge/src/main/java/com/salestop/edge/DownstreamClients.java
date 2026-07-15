package com.salestop.edge;

import com.salestop.common.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
class DownstreamClients {
  private final WebClient catalog;
  private final WebClient promo;
  private final String internalKey;
  private final ManagedChannel inventoryChannel;
  private final InventoryReservationServiceGrpc.InventoryReservationServiceBlockingStub inventory;

  DownstreamClients(
      WebClient.Builder builder,
      @Value("${edge.inventory-grpc-host:localhost}") String inventoryHost,
      @Value("${edge.inventory-grpc-port:9095}") int inventoryPort,
      @Value("${edge.catalog-url:http://localhost:8082}") String catalogUrl,
      @Value("${edge.promo-url:http://localhost:8083}") String promoUrl,
      @Value("${salestop.internal-key:local-internal-key}") String internalKey) {
    this.catalog = builder.baseUrl(catalogUrl).build();
    this.promo = builder.baseUrl(promoUrl).build();
    this.internalKey = internalKey;
    this.inventoryChannel =
        NettyChannelBuilder.forAddress(inventoryHost, inventoryPort).usePlaintext().build();
    this.inventory = InventoryReservationServiceGrpc.newBlockingStub(inventoryChannel);
  }

  Mono<Product> product(long id) {
    return catalog.get().uri("/api/v1/products/{id}", id).retrieve().bodyToMono(Product.class);
  }

  Mono<PromoResult> validatePromo(String code, BigDecimal subtotal) {
    if (code == null || code.isBlank())
      return Mono.just(
          new PromoResult(true, "No coupon", BigDecimal.ZERO, BigDecimal.ZERO, subtotal));
    return promo
        .post()
        .uri("/api/v1/coupons/validate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new PromoRequest(code, subtotal))
        .retrieve()
        .bodyToMono(PromoResult.class);
  }

  Mono<Reservation> reserve(Reserve request) {
    return Mono.fromCallable(
            () -> {
              var response =
                  inventory
                      .withDeadlineAfter(500, TimeUnit.MILLISECONDS)
                      .reserve(
                          ReserveInventoryRequest.newBuilder()
                              .setProductId(request.productId())
                              .setUserId(request.userId().toString())
                              .setQuantity(request.quantity())
                              .setIdempotencyKey(request.idempotencyKey())
                              .setProposedOrderId(request.orderId().toString())
                              .setInternalKey(internalKey)
                              .build());
              return new Reservation(
                  response.getStatus(),
                  response.getOrderId().isBlank() ? null : UUID.fromString(response.getOrderId()),
                  response.getRemainingStock());
            })
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
  }

  Mono<Void> release(Release request) {
    return Mono.fromRunnable(
            () ->
                inventory
                    .withDeadlineAfter(500, TimeUnit.MILLISECONDS)
                    .release(
                        ReleaseInventoryRequest.newBuilder()
                            .setProductId(request.productId())
                            .setUserId(request.userId().toString())
                            .setQuantity(request.quantity())
                            .setIdempotencyKey(request.idempotencyKey())
                            .setOrderId(request.orderId().toString())
                            .setInternalKey(internalKey)
                            .build()))
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        .then()
        .onErrorResume(ex -> Mono.empty());
  }

  @PreDestroy
  void close() {
    inventoryChannel.shutdown();
  }

  record Product(long id, String title, BigDecimal price, int initialStock, boolean active) {}

  record PromoRequest(String code, BigDecimal subtotal) {}

  record PromoResult(
      boolean valid,
      String reason,
      BigDecimal discountPercent,
      BigDecimal discountAmount,
      BigDecimal finalTotal) {}

  record Reserve(long productId, UUID userId, int quantity, String idempotencyKey, UUID orderId) {}

  record Reservation(String status, UUID orderId, int remainingStock) {}

  record Release(long productId, UUID userId, int quantity, String idempotencyKey, UUID orderId) {}
}
