# User & Identity Service

Spring Boot service for user registration, authentication, JWT signing, and public-key distribution.

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

- `GetPublicKey` returns the active RSA public key and JWKS payload.
- `ValidateToken` validates an access token for internal callers such as ingestion-edge.

## Local Run

```powershell
mvn -pl services/user-identity spring-boot:run
```

## Test

```powershell
mvn -pl services/user-identity test
```
