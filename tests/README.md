# System tests

- [`load/checkout.js`](load/checkout.js) is the parameterized k6 arrival-rate scenario.
- [`integration/verify-inventory.sql`](integration/verify-inventory.sql) verifies post-run stock/order consistency.
- Service-level Java tests live beside each service under `src/test` and run through `mvn verify`.
