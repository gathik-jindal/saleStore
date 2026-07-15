package com.salestop.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salestop.common.events.*;
import java.math.BigDecimal;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class OrderIntentConsumer {
  private final OrderRepository orders;
  private final OutboxRepository outbox;
  private final ObjectMapper mapper;

  OrderIntentConsumer(OrderRepository orders, OutboxRepository outbox, ObjectMapper mapper) {
    this.orders = orders;
    this.outbox = outbox;
    this.mapper = mapper;
  }

  @KafkaListener(topics = EventTopics.ORDER_INTENTS, groupId = "order-processing")
  @Transactional
  public void consume(OrderIntentEvent event) throws JsonProcessingException {
    if (orders.existsById(event.orderId()) || orders.existsByIdempotencyKey(event.idempotencyKey()))
      return;
    BigDecimal total = event.unitPrice().multiply(BigDecimal.valueOf(event.quantity()));
    OrderEntity order =
        orders.save(
            new OrderEntity(
                event.orderId(),
                event.productId(),
                event.userId(),
                event.quantity(),
                event.idempotencyKey(),
                total,
                event.reservedAt()));
    OrderCreatedEvent created =
        new OrderCreatedEvent(
            order.id(),
            order.productId(),
            order.userId(),
            order.quantity(),
            order.total(),
            order.createdAt());
    outbox.save(
        new OutboxEvent(
            EventTopics.ORDER_CREATED, order.id().toString(), mapper.writeValueAsString(created)));
  }
}
