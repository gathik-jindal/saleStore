package com.salestop.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
class Payment {
  @Id private UUID id;

  @Column(name = "order_id", nullable = false, unique = true)
  private UUID orderId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private String status;

  @Column(name = "provider_reference", unique = true)
  private String providerReference;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Payment() {}

  Payment(UUID orderId, UUID userId, BigDecimal amount) {
    this.id = UUID.randomUUID();
    this.orderId = orderId;
    this.userId = userId;
    this.amount = amount;
    this.status = "PENDING";
    this.updatedAt = Instant.now();
  }

  void transition(String status, String ref) {
    if (!this.status.equals(status)) {
      this.status = status;
      this.providerReference = ref;
      this.updatedAt = Instant.now();
    }
  }

  UUID id() {
    return id;
  }

  UUID orderId() {
    return orderId;
  }

  UUID userId() {
    return userId;
  }

  BigDecimal amount() {
    return amount;
  }

  String status() {
    return status;
  }

  String reference() {
    return providerReference;
  }

  Instant updatedAt() {
    return updatedAt;
  }
}
