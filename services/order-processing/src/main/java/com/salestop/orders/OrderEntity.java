package com.salestop.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderEntity {
  @Id private UUID id;

  @Column(name = "product_id", nullable = false)
  private long productId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
  private String idempotencyKey;

  @Column(nullable = false, length = 50)
  private String status;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal total;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OrderEntity() {}

  OrderEntity(
      UUID id,
      long productId,
      UUID userId,
      int quantity,
      String idempotencyKey,
      BigDecimal total,
      Instant createdAt) {
    this.id = id;
    this.productId = productId;
    this.userId = userId;
    this.quantity = quantity;
    this.idempotencyKey = idempotencyKey;
    this.total = total;
    this.createdAt = createdAt;
    this.status = "PENDING_PAYMENT";
  }

  UUID id() {
    return id;
  }

  long productId() {
    return productId;
  }

  UUID userId() {
    return userId;
  }

  int quantity() {
    return quantity;
  }

  String key() {
    return idempotencyKey;
  }

  String status() {
    return status;
  }

  BigDecimal total() {
    return total;
  }

  Instant createdAt() {
    return createdAt;
  }

  void paymentStatus(String paymentStatus) {
    this.status =
        switch (paymentStatus) {
          case "SUCCEEDED" -> "PAID";
          case "FAILED" -> "PAYMENT_FAILED";
          case "REFUNDED" -> "REFUNDED";
          default -> this.status;
        };
  }
}
