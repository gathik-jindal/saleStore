# TicketStop — Distributed Flash-Sale Engine

Repository scaffold for a Kubernetes-native, high-throughput flash-sale platform.

## Project summary

TicketStop implements a decoupled, event-driven architecture tailored for flash-sale workloads. It provides an ultra-low latency ingestion path that atomically reserves inventory via Redis, and an asynchronous background pipeline that converts reservations to durable orders in PostgreSQL. The design targets sub-second reservation latency, zero oversell, and horizontal scalability.

## Key features

- Stateless Ingestion Edge that validates auth and reserves stock atomically
- Redis-backed Inventory Cache with Lua script atomics and idempotency
- Kafka topics for `order_intents` and `order.created` events
- Order Processing micro-batches that persist to PostgreSQL
- Payment and Notification services for completion workflows
- Kubernetes + KEDA autoscaling strategies and circuit-breaker driven resilience

## Quick start (developer)

Prerequisites:

- Docker / Docker Desktop
- kubectl and a Kubernetes cluster (minikube, kind, or cloud)
- Kafka, Redis, and PostgreSQL (local docker-compose or testcontainers recommended)

Local quick-start (example using docker-compose):

```powershell
# from repo root
scripts\local_start.sh
# seed a flash-sale catalog
scripts\seed_flashsale.sh --product-id=1 --stock=100
# run load test (k6) in another terminal
scripts\run_k6.sh --script=scripts/k6/checkout.js
```

## Folder layout

- `services/` — per-service folders (each with a README explaining responsibilities)
  - `services/ingestion-edge/` — fast HTTP entrypoint
  - `services/inventory-cache/` — Redis + Lua scripts
  - `services/order-processing/` — Kafka consumer + DB writes
  - `services/payment-gateway/`, `services/notification/`, etc.
- `cmd/` — service entrypoint examples
- `configs/` — environment configurations and k8s overlays
- `infra/k8s/` — Kubernetes manifests and Helm charts
- `scripts/` — useful dev/CI helper scripts
- `docs/` — architecture, load-testing, and runbooks
- `sql/` — schema and migration templates
- `docker/` — Dockerfile templates
- `pkg/common/` — cross-service shared code and DTOs
- `tests/` — integration and load test harnesses
