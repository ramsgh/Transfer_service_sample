# ADR 0002: Start as one deployable with clear module boundaries

- Status: Accepted
- Context: A small team needs to deliver quickly, while the product is
  expected to grow into several services (accounts, payments, notifications).
- Decision: One Spring Boot service now, with accounts and transfers kept
  as separate modules (no cross-module repository access). Extract a
  service only when it needs independent scaling or release cadence, and
  introduce events (e.g. Kafka) at that boundary.
- Consequences: Simple transactions and operations today; a clean path to
  microservices later.
