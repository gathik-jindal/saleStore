# User & Identity Service

Spring Boot WebFlux service for user registration, authentication, JWT signing, and public-key distribution.

## Runtime

- HTTP: `8081`
- gRPC: `9091`
- Default local database: in-memory H2 using PostgreSQL compatibility mode
- Production database: set `IDENTITY_DATASOURCE_URL`, `IDENTITY_DATASOURCE_USERNAME`, and `IDENTITY_DATASOURCE_PASSWORD`

## HTTP API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /.well-known/jwks.json`

## gRPC API

The protobuf contract lives at `src/main/proto/identity_key_service.proto`.

- `GetPublicKey` returns the active public key and JWKS payload for edge-side caches.
- `ValidateToken` remains available for diagnostics and internal fallback paths, but checkout traffic should verify JWTs locally with cached public keys instead of calling this per request.

## JWT Verification Model

The service signs access tokens with RS256 today. The architecture allows ES256 as a future signing profile. Downstream services should cache JWKS keys by `kid`, refresh them periodically, and locally verify signature, issuer, audience, and expiry on the hot path.

## Local Run

```powershell
mvn -pl services/user-identity spring-boot:run
```

## Test

```powershell
mvn -pl services/user-identity test
```
