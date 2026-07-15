package com.salestop.orders;

import com.salestop.common.events.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class OrderStatusConsumer {
  private final OrderRepository orders;

  OrderStatusConsumer(OrderRepository orders) {
    this.orders = orders;
  }

  @KafkaListener(topics = EventTopics.PAYMENT_STATUS, groupId = "order-status")
  @Transactional
  public void paymentStatus(PaymentStatusEvent event) {
    orders.findById(event.orderId()).ifPresent(order -> order.paymentStatus(event.status()));
  }
}
