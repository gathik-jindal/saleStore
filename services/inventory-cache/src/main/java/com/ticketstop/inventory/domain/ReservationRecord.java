package com.ticketstop.inventory.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation_records")
public class ReservationRecord {
    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ReservationRecord() {
    }

    public ReservationRecord(UUID id, String idempotencyKey, UUID productId, String userId, int quantity,
                             ReservationStatus status, OffsetDateTime expiresAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }

    public boolean isActive(OffsetDateTime now) {
        return status == ReservationStatus.RESERVED && expiresAt.isAfter(now);
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getProductId() { return productId; }
    public String getUserId() { return userId; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
}
