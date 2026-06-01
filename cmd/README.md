# `cmd/` — service entrypoints

This folder will contain per-service command entrypoints, e.g.:

- `cmd/ingestion/main.go` — starts HTTP + gRPC server for ingestion
- `cmd/order/main.go` — starts Kafka consumer worker

Each entry should wire configuration, logging, metrics, and graceful shutdown.
