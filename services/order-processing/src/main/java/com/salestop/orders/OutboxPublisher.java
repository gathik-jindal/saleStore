package com.salestop.orders;

import com.fasterxml.jackson.databind.*;
import com.salestop.common.events.*;
import java.time.Duration;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class OutboxPublisher {
  private final OutboxRepository outbox;
  private final KafkaTemplate<String, Object> kafka;
  private final ObjectMapper mapper;

  OutboxPublisher(
      OutboxRepository outbox, KafkaTemplate<String, Object> kafka, ObjectMapper mapper) {
    this.outbox = outbox;
    this.kafka = kafka;
    this.mapper = mapper;
  }

  @Scheduled(fixedDelayString = "${orders.outbox-poll-ms:250}")
  @Transactional
  public void publish() {
    for (OutboxEvent event :
        outbox.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))) {
      try {
        Object payload =
            EventTopics.ORDER_CREATED.equals(event.topic())
                ? mapper.readValue(event.payload(), OrderCreatedEvent.class)
                : mapper.readTree(event.payload());
        kafka
            .send(event.topic(), event.eventKey(), payload)
            .get(Duration.ofSeconds(3).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        event.published();
      } catch (Exception ex) {
        break;
      }
    }
  }
}
