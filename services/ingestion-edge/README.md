# Ingestion Edge

Reactive checkout entry point on port 8084. It applies a Redis token bucket, verifies cached RS256 JWKS keys locally, loads product/promo data, reserves inventory over gRPC, and durably publishes an order intent. See the root [API reference](../../docs/API.md).
