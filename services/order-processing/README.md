# Order Processing

Kafka worker and query API on port 8086. It persists each intent once, commits an outbox row with the order, publishes after commit, and routes exhausted failures to `order.intents.dlq.v1`.
