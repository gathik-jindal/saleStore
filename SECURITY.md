# Security policy

Please do not open public issues for suspected vulnerabilities. Use GitHub's private vulnerability reporting feature for this repository and include reproduction steps, affected endpoints, and impact. Supported security fixes target the latest `main` branch.

Development defaults in `.env.example` are intentionally non-production values. Rotate every admin, internal, database, and webhook secret before deployment; terminate TLS at the ingress; use a managed secret store; and persist the identity signing key outside the container.
