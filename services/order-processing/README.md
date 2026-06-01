# Order Processing Service

Responsibilities:

- Consume `order_intents` from Kafka in micro-batches
- Persist orders into PostgreSQL ledger tables
- Emit `order.created` events after durable commit

Planned contents:

- Kafka consumer worker code
- Database transaction and retry logic
- DLQ handling and monitoring
