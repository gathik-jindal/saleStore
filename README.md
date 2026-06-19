# saleStop — Distributed Flash-Sale Engine

Spring Boot microservices for a Kubernetes-native, high-throughput flash-sale platform.

## Project Summary

saleStop implements a decoupled, event-driven architecture tailored for flash-sale workloads. It provides a low-latency ingestion path that atomically reserves inventory through Redis, and an asynchronous background pipeline that converts reservations to durable orders in PostgreSQL.

## Spring Boot Layout

This repository is now organized as a Maven multi-module project:

- `pom.xml` - parent build and shared dependency versions
- `services/user-identity` - implemented Spring Boot identity service
- `services/*` - future Spring Boot service modules
- `infra/`, `docker/`, `configs/` - deployment and runtime configuration
- `docs/` - architecture, requirements, and load-testing notes

The older generic scaffold folders are being retired in favor of per-service Spring Boot modules.

## Implemented Service

`services/user-identity` is complete enough for local development:

- User registration and login
- BCrypt password hashing
- RS256-signed JWT access tokens with ES256-ready architecture
- Refresh token sessions with rotation and logout revocation
- `/.well-known/jwks.json`
- JWKS/gRPC public-key publishing for local edge-side JWT verification
- Flyway schema migration
- H2 local profile by default, PostgreSQL via environment variables

## Quick Start

Prerequisites:

- Java 21
- Maven 3.9+
- Docker / Docker Desktop for infrastructure dependencies
- kubectl and a Kubernetes cluster when running the full mesh

Run the implemented identity service:

```powershell
mvn -pl services/user-identity spring-boot:run
```

HTTP starts on `8081`; the internal gRPC key service starts on `9091`.

Run tests:

```powershell
mvn -pl services/user-identity test
```

## Folder Layout

- `services/` - per-service Spring Boot modules
  - `services/user-identity/` - WebFlux registration, login, JWT signing, refresh sessions, JWKS, gRPC key API
  - `services/ingestion-edge/` - future fast HTTP entrypoint
  - `services/inventory-cache/` - future Redis + Lua wrapper
  - `services/order-processing/` - future Kafka consumer + DB writes
  - `services/payment-gateway/`, `services/notification/`, etc.
- `configs/` - environment configurations and Kubernetes overlays
- `infra/k8s/` - Kubernetes manifests and Helm charts
- `scripts/` - useful dev and CI helper scripts
- `docs/` - architecture, load-testing, and runbooks
- `sql/` - reference SQL; service migrations live inside each service
- `docker/` - Dockerfile templates
- `tests/` - integration and load test harnesses
