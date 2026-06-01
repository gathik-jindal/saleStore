# User & Identity Service

Responsibilities:

- User registration and authentication
- JWT signing and key distribution (gRPC public key endpoint)
- Session and token lifecycle management

Files to include:

- `cmd/` entry for HTTP + gRPC server
- `pkg/auth` for token handling
- Database migrations and tests

(Security and key-rotation guidance goes here.)
