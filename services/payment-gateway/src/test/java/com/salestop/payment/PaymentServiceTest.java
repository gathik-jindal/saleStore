package com.salestop.payment;

import static org.mockito.Mockito.*;

import com.salestop.common.events.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class PaymentServiceTest {
  @Test
  void createsOnePaymentAndPublishesItsStatus() {
    PaymentRepository repo = mock(PaymentRepository.class);
    @SuppressWarnings("unchecked")
    KafkaTemplate<String, Object> kafka = mock(KafkaTemplate.class);
    when(repo.save(any())).thenAnswer(c -> c.getArgument(0));
    OrderCreatedEvent event =
        new OrderCreatedEvent(
            UUID.randomUUID(), 1, UUID.randomUUID(), 1, BigDecimal.valueOf(99), Instant.now());
    new PaymentService(repo, kafka).orderCreated(event);
    verify(repo).save(any(Payment.class));
    verify(kafka)
        .send(
            eq(EventTopics.PAYMENT_STATUS),
            eq(event.orderId().toString()),
            any(PaymentStatusEvent.class));
  }
}
