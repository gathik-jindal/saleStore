package com.salestop.payment;

import com.salestop.common.events.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PaymentService {
  private final PaymentRepository payments;
  private final KafkaTemplate<String, Object> kafka;

  PaymentService(PaymentRepository payments, KafkaTemplate<String, Object> kafka) {
    this.payments = payments;
    this.kafka = kafka;
  }

  @KafkaListener(topics = EventTopics.ORDER_CREATED, groupId = "payment-gateway")
  @Transactional
  public void orderCreated(OrderCreatedEvent event) {
    if (payments.findByOrderId(event.orderId()).isPresent()) return;
    Payment payment = payments.save(new Payment(event.orderId(), event.userId(), event.total()));
    String reference = "demo_" + UUID.randomUUID();
    payment.transition("SUCCEEDED", reference);
    kafka.send(
        EventTopics.PAYMENT_STATUS,
        event.orderId().toString(),
        new PaymentStatusEvent(
            event.orderId(), event.userId(), payment.status(), reference, Instant.now()));
  }

  @Transactional
  Payment update(UUID orderId, String status, String reference) {
    Payment payment = payments.findByOrderId(orderId).orElseThrow();
    payment.transition(status, reference);
    kafka.send(
        EventTopics.PAYMENT_STATUS,
        orderId.toString(),
        new PaymentStatusEvent(orderId, payment.userId(), status, reference, Instant.now()));
    return payment;
  }
}
