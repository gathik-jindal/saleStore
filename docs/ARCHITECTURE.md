# Architecture Overview

This document will explain the high-level distributed architecture for the flash-sale engine:

- Service topology (8-service mesh)
- Synchronous ingestion path vs asynchronous background convergence
- Communication channels (gRPC, Kafka, Redis, PostgreSQL)
- Scaling, observability and resiliency patterns

(Place design diagrams and sequence flows here.)
