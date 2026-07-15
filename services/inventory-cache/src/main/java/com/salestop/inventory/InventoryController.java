package com.salestop.inventory;

import com.salestop.common.events.InventorySeededEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class InventoryController {
  private final ReservationService reservations;
  private final String internalKey;

  InventoryController(
      ReservationService reservations,
      @Value("${salestop.internal-key:local-internal-key}") String internalKey) {
    this.reservations = reservations;
    this.internalKey = internalKey;
  }

  @PostMapping("/internal/v1/reservations")
  ReservationResponse reserve(
      @RequestHeader("X-Internal-Key") String key, @Valid @RequestBody ReservationRequest request) {
    authorize(key);
    UUID proposed = request.orderId() == null ? UUID.randomUUID() : request.orderId();
    var result =
        reservations.reserve(
            request.productId(),
            request.userId(),
            request.quantity(),
            request.idempotencyKey(),
            proposed);
    return new ReservationResponse(
        result.status().name(), result.orderId(), result.remainingStock());
  }

  @PostMapping("/internal/v1/reservations/release")
  ReleaseResponse release(
      @RequestHeader("X-Internal-Key") String key, @Valid @RequestBody ReleaseRequest request) {
    authorize(key);
    return new ReleaseResponse(
        reservations.release(
            request.productId(),
            request.userId(),
            request.quantity(),
            request.idempotencyKey(),
            request.orderId()));
  }

  @GetMapping("/api/v1/inventory/{productId}")
  InventoryResponse availability(@PathVariable long productId) {
    int stock = reservations.availability(productId);
    if (stock < 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not seeded");
    return new InventoryResponse(productId, stock, stock > 0);
  }

  @PostMapping("/internal/v1/inventory/{productId}/seed")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void seed(
      @RequestHeader("X-Internal-Key") String key,
      @PathVariable long productId,
      @Valid @RequestBody SeedRequest request) {
    authorize(key);
    reservations.seed(productId, request.stock());
  }

  @KafkaListener(topics = "inventory.seeded.v1", groupId = "inventory-cache")
  void onSeed(InventorySeededEvent event) {
    reservations.seed(event.productId(), event.stock());
  }

  private void authorize(String key) {
    if (!internalKey.equals(key))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal key");
  }

  record ReservationRequest(
      @Positive long productId,
      @NotNull UUID userId,
      @Min(1) @Max(10) int quantity,
      @NotBlank @Size(max = 255) String idempotencyKey,
      UUID orderId) {}

  record ReservationResponse(String status, UUID orderId, int remainingStock) {}

  record ReleaseRequest(
      @Positive long productId,
      @NotNull UUID userId,
      @Min(1) int quantity,
      @NotBlank String idempotencyKey,
      @NotNull UUID orderId) {}

  record ReleaseResponse(boolean released) {}

  record SeedRequest(@Min(0) int stock) {}

  record InventoryResponse(long productId, int availableStock, boolean available) {}
}
