# Architecture

saleStop is an eight-service, event-driven flash-sale platform. The design keeps the synchronous acceptance path short and moves durable business processing to Kafka consumers.

![Architecture diagram](architecture.png)

## Checkout sequence

1. The edge applies a Redis token bucket keyed by client IP.
2. It verifies the RS256 access token in-process. Public keys are cached from identity JWKS and refreshed by TTL or an unknown `kid`.
3. Catalog provides the active product and price; promo validates an optional coupon.
4. Inventory executes one Lua script over the stock, idempotency, and buyer keys. Redis serializes the script, so concurrent callers cannot oversell.
5. The edge publishes `order.intents.v1` with Kafka `acks=all` and an idempotent producer, then returns `202`.
6. If durable publication fails, the edge calls a compare-and-release Lua operation before returning `503`.

The edge-to-inventory reservation uses gRPC on port 9095. The inventory HTTP internal API exists for operations and diagnostics, not the normal checkout path.

## Asynchronous convergence

Order processing consumes intent events at least once. It checks both order ID and idempotency key, commits the order and an outbox row in one PostgreSQL transaction, then publishes `order.created.v1`. Poison messages retry four times and move to `order.intents.dlq.v1`.

Payment consumes order-created events, runs a deterministic demo provider adapter, stores the state, and publishes `payment.status.v1`. Notification retains the latest 50 user events in memory and pushes live updates over server-sent events.

## Consistency model

| Boundary | Guarantee |
| --- | --- |
| Concurrent stock reservation | Atomic Redis script; stock never decrements below requested quantity |
| Client retry | Same idempotency key returns the original order ID for five minutes |
| Repeat buyer | Product/user guard rejects a second reservation during the sale window |
| Kafka delivery | At least once; consumers must be idempotent |
| Order DB → Kafka | Transactional outbox prevents a committed order without a publishable event |
| Edge Kafka failure | Best-effort compare-and-release compensation; an operator can reconcile rare process-crash gaps |

No system spanning Redis and Kafka can be made atomic without an additional coordination protocol. The implementation makes the remaining failure window explicit and observable instead of claiming distributed exactly-once behavior.

## Storage ownership

- Identity DB: users and hashed refresh sessions.
- Catalog DB: products and initial stock.
- Promo DB: coupon rules and counters.
- Redis: live stock, rate buckets, idempotency responses, and buyer guards.
- Order DB: durable order ledger and outbox.
- Payment DB: payment state and provider references.
- Notification: deliberately ephemeral in this reference implementation.

Flyway owns relational schemas. Services never read another service's database in normal operation.

## Scaling and resilience

The Helm chart starts edge and inventory at three replicas and applies CPU HPA at 70% with a 200%/10-second scale-up policy. KEDA scales order workers from Kafka consumer lag. Containers run as non-root with dropped Linux capabilities, read-only root filesystems, resource limits, and readiness/liveness probes.

Prometheus scrapes Actuator metrics. Production topology should add OpenTelemetry traces, centralized logs, Redis Cluster, replicated Kafka, managed PostgreSQL, TLS/mTLS, and external secret/signing-key management.

## Event contracts

Topics are versioned in names and Java records live in `pkg/common`. The protobuf reservation contract is also generated from that module. Breaking changes require a new topic/API version; compatible fields may be added with tolerant consumers.
