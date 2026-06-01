# Ingestion Edge Service

This service is the ultra-low-latency entry point for checkout requests.

Contents:

- High-level responsibility: validate auth, rate-limit, call Inventory Cache Service via gRPC, publish intent to Kafka.
- API surface: `POST /checkout` and health endpoints.
- Runtime: stateless; recommended container image and resource hints.
- Configuration: environment variables for gRPC endpoints, Redis, Kafka, rate limits.

(Implementation notes and examples go here.)
