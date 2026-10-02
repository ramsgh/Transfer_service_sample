# Architecture overview

    Client --> REST API (Spring Boot)  --> PostgreSQL
                  |  /api/v1/accounts
                  |  /api/v1/transfers   (Idempotency-Key header)
                  |
              Actuator probes --> Kubernetes liveness / readiness

## Transfer flow
1. Look up `Idempotency-Key`; replay returns the stored result.
2. Validate (different accounts, currency, 2-decimal amount).
3. Lock both accounts in id order.
4. Debit, credit, save transfer and audit entry - one transaction.

## Quality attributes and next steps
| Concern | Now | Next |
|---|---|---|
| Consistency | Atomic transaction, row locks | Outbox pattern to publish events reliably |
| Reliability | Idempotency, health probes, rolling updates | Retries with backoff, DR plan |
| Security | Non-root container, input validation | OAuth2/JWT, secrets manager, mTLS |
| Observability | Actuator health, audit trail | Metrics, tracing, log aggregation |
| Data | JPA auto-DDL (demo only) | Flyway migrations |
| Scale | Stateless pods + HPA | Read replicas, ledger model for hot accounts |
