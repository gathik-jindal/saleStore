package com.salestop.notification;

import java.time.Duration;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController {
  private final NotificationHub hub;

  NotificationController(NotificationHub hub) {
    this.hub = hub;
  }

  @GetMapping
  List<NotificationHub.UserNotification> recent(@RequestParam UUID userId) {
    return hub.recent(userId);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<ServerSentEvent<?>> stream(@RequestParam UUID userId) {
    Flux<ServerSentEvent<?>> events =
        hub.stream(userId)
            .map(
                n ->
                    ServerSentEvent.builder(n).id(n.id().toString()).event("order-status").build());
    Flux<ServerSentEvent<?>> heartbeat =
        Flux.interval(Duration.ofSeconds(15))
            .map(i -> ServerSentEvent.builder().comment("keep-alive").build());
    return Flux.merge(events, heartbeat);
  }
}
