# API reference

All request and response bodies use JSON. Local defaults are shown; production callers should use TLS and route public endpoints through an API gateway.

## Identity — `:8081`

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Create a user and issue access/refresh tokens |
| POST | `/api/v1/auth/login` | Authenticate and issue tokens |
| POST | `/api/v1/auth/refresh` | Rotate a usable refresh token |
| POST | `/api/v1/auth/logout` | Revoke a refresh session |
| GET | `/api/v1/auth/me` | Resolve the bearer token's user |
| GET | `/.well-known/jwks.json` | Publish active public signing keys |

## Catalog — `:8082`

`GET /api/v1/products` and `GET /api/v1/products/{id}` are public. `POST /api/v1/products`, `PUT /api/v1/products/{id}`, and `POST /api/v1/products/{id}/seed` require `X-Admin-Key`. Creating or explicitly seeding a product publishes `inventory.seeded.v1`.

## Coupons — `:8083`

`POST /api/v1/coupons/validate` accepts `{code, subtotal}`. Admin creation at `POST /api/v1/coupons` requires `X-Admin-Key`.

## Edge — `:8084`

`POST /api/v1/checkout` requires a bearer token and `Idempotency-Key`. Body fields are `productId`, `quantity` (1–10), and optional `promoCode`.

## Inventory — `:8085` / gRPC `:9095`

`GET /api/v1/inventory/{productId}` exposes current cached availability. Reservation and release are internal gRPC operations defined in [`inventory_reservation.proto`](../pkg/common/src/main/proto/inventory_reservation.proto). HTTP internal endpoints are retained for operations and diagnostics and require `X-Internal-Key`.

## Orders — `:8086`

`GET /api/v1/orders/{id}` retrieves an order; `GET /api/v1/orders?userId=<uuid>` lists a user's orders newest first.

## Payments — `:8087`

`GET /api/v1/payments/orders/{orderId}` retrieves payment state. `POST /api/v1/payments/webhooks/provider` accepts `{orderId,status,providerReference}` and requires `X-Webhook-Secret`.

## Notifications — `:8088`

`GET /api/v1/notifications?userId=<uuid>` returns the last 50 in-memory events. `GET /api/v1/notifications/stream?userId=<uuid>` is an SSE stream with 15-second heartbeats.

Every service exposes `/actuator/health`; services with the Prometheus registry expose `/actuator/prometheus`.
