package com.salestop.orders;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
class OutboxEvent {
  @Id private UUID id;

  @Column(nullable = false)
  private String topic;

  @Column(name = "event_key", nullable = false)
  private String eventKey;

  @Lob
  @Column(nullable = false)
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  protected OutboxEvent() {}

  OutboxEvent(String topic, String eventKey, String payload) {
    this.id = UUID.randomUUID();
    this.topic = topic;
    this.eventKey = eventKey;
    this.payload = payload;
    this.createdAt = Instant.now();
  }

  UUID id() {
    return id;
  }

  String topic() {
    return topic;
  }

  String eventKey() {
    return eventKey;
  }

  String payload() {
    return payload;
  }

  void published() {
    this.publishedAt = Instant.now();
  }
}
