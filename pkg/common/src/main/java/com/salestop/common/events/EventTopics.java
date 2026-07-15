package com.salestop.common.events;

public final class EventTopics {
  public static final String ORDER_INTENTS = "order.intents.v1";
  public static final String ORDER_CREATED = "order.created.v1";
  public static final String PAYMENT_STATUS = "payment.status.v1";
  public static final String INVENTORY_SEEDED = "inventory.seeded.v1";
  public static final String ORDER_INTENTS_DLQ = "order.intents.dlq.v1";

  private EventTopics() {}
}
