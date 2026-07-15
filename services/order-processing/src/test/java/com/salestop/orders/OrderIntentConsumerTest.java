package com.salestop.orders;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salestop.common.events.OrderIntentEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderIntentConsumerTest {
  @Test
  void persistsOneOrderAndOneOutboxEvent() throws Exception {
    OrderRepository orders = mock(OrderRepository.class);
    OutboxRepository outbox = mock(OutboxRepository.class);
    when(orders.save(any())).thenAnswer(call -> call.getArgument(0));
    OrderIntentEvent event =
        new OrderIntentEvent(
            UUID.randomUUID(),
            42,
            UUID.randomUUID(),
            2,
            "retry-key",
            null,
            BigDecimal.valueOf(25),
            Instant.now());
    new OrderIntentConsumer(orders, outbox, new ObjectMapper().findAndRegisterModules())
        .consume(event);
    verify(orders).save(any(OrderEntity.class));
    verify(outbox).save(any(OutboxEvent.class));
  }

  @Test
  void ignoresAReplay() throws Exception {
    OrderRepository orders = mock(OrderRepository.class);
    OutboxRepository outbox = mock(OutboxRepository.class);
    OrderIntentEvent event =
        new OrderIntentEvent(
            UUID.randomUUID(),
            42,
            UUID.randomUUID(),
            1,
            "retry-key",
            null,
            BigDecimal.TEN,
            Instant.now());
    when(orders.existsById(event.orderId())).thenReturn(true);
    new OrderIntentConsumer(orders, outbox, new ObjectMapper()).consume(event);
    verify(orders, never()).save(any());
    verifyNoInteractions(outbox);
  }

  @Test
  void appliesPaymentStateToTheOrder() {
    OrderRepository orders = mock(OrderRepository.class);
    OrderEntity order =
        new OrderEntity(
            UUID.randomUUID(), 1, UUID.randomUUID(), 1, "key", BigDecimal.TEN, Instant.now());
    when(orders.findById(order.id())).thenReturn(java.util.Optional.of(order));
    new OrderStatusConsumer(orders)
        .paymentStatus(
            new com.salestop.common.events.PaymentStatusEvent(
                order.id(), order.userId(), "SUCCEEDED", "demo", Instant.now()));
    org.assertj.core.api.Assertions.assertThat(order.status()).isEqualTo("PAID");
  }
}
