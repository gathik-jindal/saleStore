package com.salestop.notification;

import com.salestop.common.events.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.*;

@Component
class NotificationHub {
  private final Map<UUID, Sinks.Many<UserNotification>> streams = new ConcurrentHashMap<>();
  private final Map<UUID, Deque<UserNotification>> recent = new ConcurrentHashMap<>();

  @KafkaListener(topics = EventTopics.PAYMENT_STATUS, groupId = "notification")
  public void payment(PaymentStatusEvent event) {
    UserNotification notification =
        new UserNotification(
            UUID.randomUUID(),
            event.userId(),
            event.orderId(),
            "PAYMENT_" + event.status(),
            "Payment for order " + event.orderId() + " is " + event.status().toLowerCase(),
            event.occurredAt());
    Deque<UserNotification> queue =
        recent.computeIfAbsent(event.userId(), ignored -> new ConcurrentLinkedDeque<>());
    queue.addFirst(notification);
    while (queue.size() > 50) queue.pollLast();
    streams
        .computeIfAbsent(event.userId(), ignored -> Sinks.many().multicast().onBackpressureBuffer())
        .tryEmitNext(notification);
  }

  Flux<UserNotification> stream(UUID userId) {
    return streams
        .computeIfAbsent(userId, ignored -> Sinks.many().multicast().onBackpressureBuffer())
        .asFlux();
  }

  List<UserNotification> recent(UUID userId) {
    return List.copyOf(recent.getOrDefault(userId, new ConcurrentLinkedDeque<>()));
  }

  record UserNotification(
      UUID id, UUID userId, UUID orderId, String type, String message, Instant occurredAt) {}
}
