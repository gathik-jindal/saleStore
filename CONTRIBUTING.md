# Contributing

Use Java 21 and Maven 3.9+. Create a focused branch, add tests for behavior changes, run `mvn verify`, and open a pull request using the repository template. Service-owned schema changes must be additive Flyway migrations; never edit an already-released migration. Event changes require a new versioned topic or a backward-compatible contract update.

Keep the synchronous checkout path free from database calls and avoid network authentication calls per request. New consumers must be idempotent because Kafka delivery is at least once.
