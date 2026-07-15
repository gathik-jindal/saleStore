# Local development

## Fast feedback

Run all unit and application tests with `mvn verify`. Run one service with `mvn -pl services/<service> -am spring-boot:run`. JPA services default to isolated PostgreSQL-compatible H2 databases; Kafka and Redis services expect the infrastructure endpoints configured in their `application.yml`.

Start only infrastructure when developing services in the IDE:

```powershell
docker compose up -d postgres redis kafka
```

Set the relevant datasource URL/driver variables from [`compose.yml`](../compose.yml), then start services in port order. The full stack is simpler for end-to-end work: `docker compose up --build -d` followed by `./scripts/smoke-test.ps1`.

## Useful checks

```powershell
Invoke-RestMethod http://localhost:8084/actuator/health
Invoke-RestMethod http://localhost:8085/api/v1/inventory/1
docker compose logs -f ingestion-edge inventory-cache order-processing
```

## Configuration rules

- Never commit `.env`; use `.env.example` as the key list.
- Development secrets are conveniences, not deployable credentials.
- Each service owns its Flyway migrations and database.
- Treat shared Kafka records and protobuf files as public compatibility contracts.
- A consumer may see an event more than once and must remain idempotent.

## Kubernetes rendering

Install Helm 3 and render before opening a deployment pull request:

```powershell
helm lint infra/helm/salestop
helm template salestop infra/helm/salestop --set secrets.postgresPassword=test
```

KEDA must be installed in the target cluster for the order-processing `ScaledObject`.
