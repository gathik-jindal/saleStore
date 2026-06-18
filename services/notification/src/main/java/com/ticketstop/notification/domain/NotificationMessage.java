package com.ticketstop.notification.domain;

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
@Table(name = "notifications")
public class NotificationMessage {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "order_id")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationChannel channel;

    @Column(nullable = false, length = 320)
    private String recipient;

    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    protected NotificationMessage() {
    }

    public NotificationMessage(UUID id, String userId, UUID orderId, NotificationChannel channel, String recipient,
                               String subject, String body) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }

    public void markDelivered() {
        status = NotificationStatus.DELIVERED;
        deliveredAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public UUID getOrderId() { return orderId; }
    public NotificationChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getDeliveredAt() { return deliveredAt; }
}
