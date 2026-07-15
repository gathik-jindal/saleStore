# Load testing

The k6 scenario in [`tests/load/checkout.js`](../tests/load/checkout.js) drives the authenticated checkout endpoint with unique idempotency keys. It treats `202` and the expected sold-out `409` as valid protocol outcomes and enforces a p99 latency threshold of 50 ms.

## Prepare

1. Deploy the stack on infrastructure sized for the intended rate. A laptop Compose run validates behavior, not the 20k target.
2. Create a user and retain its access token.
3. Create a product whose stock exceeds the number of expected accepted reservations, or intentionally test the sold-out boundary.
4. Confirm Prometheus is scraping the edge, inventory, Kafka consumer, Redis, and PostgreSQL metrics available in the environment.

## Safe local run

```powershell
$env:ACCESS_TOKEN = '<token>'
$env:PRODUCT_ID = '1'
$env:RATE = '100'
k6 run tests/load/checkout.js
```

The local defaults ramp to 100 arrivals/s over 10 seconds, sustain for 30 seconds, and ramp down over 10 seconds.

## Target cluster run

```powershell
$env:BASE_URL = 'https://api.example.com'
$env:ACCESS_TOKEN = '<token>'
$env:PRODUCT_ID = '1'
$env:RATE = '20000'
$env:RAMP_DURATION = '60s'
$env:SUSTAIN_DURATION = '180s'
$env:RAMP_DOWN_DURATION = '60s'
$env:PRE_ALLOCATED_VUS = '5000'
$env:MAX_VUS = '25000'
k6 run --summary-export=load-summary.json tests/load/checkout.js
```

Use distributed k6 execution when one generator cannot sustain the configured arrival rate. Keep generators near the cluster but outside it so they do not consume application capacity.

## Pass criteria

- Checkout HTTP p99 is at most 50 ms.
- Unexpected HTTP failures remain below 1%.
- No Redis stock value becomes negative.
- No duplicate `idempotency_key` exists in PostgreSQL.
- Order-intent consumer lag returns to zero after ramp-down.
- The DLQ remains empty for valid generated payloads.
- Accepted reservation count equals durable order count after convergence.

Run [`tests/integration/verify-inventory.sql`](../tests/integration/verify-inventory.sql) against catalog and order data exported into the same verification connection, or adapt it to a cross-database query in the target platform. A sold-out test must finish with residual stock exactly zero; every other test must finish non-negative.

## Reporting

Record the commit SHA, container image tags, cluster node types, pod limits/replicas, Redis and Kafka topology, database tier, generator topology, k6 summary, Prometheus screenshots, and consistency query output. Do not publish the 20k target as achieved until those artifacts are captured from a representative run.
