# Inventory Cache

Atomic Redis inventory service on HTTP 8085 and gRPC 9095. One Lua script combines stock deduction, idempotency response caching, and per-user purchase protection; a second script performs compare-and-release compensation.
