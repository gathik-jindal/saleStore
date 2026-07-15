# Configuration

Services use environment variables with safe H2/local defaults in their `application.yml` files. The complete key mapping is visible in [`../compose.yml`](../compose.yml); copy [`../.env.example`](../.env.example) to `.env` for local overrides. Kubernetes values and secret placeholders live in [`../infra/helm/salestop`](../infra/helm/salestop).

Never commit real credentials or environment-specific `application-*.yml` files.
