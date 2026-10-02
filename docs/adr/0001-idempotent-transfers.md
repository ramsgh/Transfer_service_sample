# ADR 0001: Idempotent, atomic transfers with pessimistic locking

- Status: Accepted
- Context: Clients and networks retry. A retried "transfer 25 EUR" request
  must never move the money twice, and a transfer must never half-complete
  (debit without credit).
- Options considered:
  1. Optimistic locking (version column) - no DB locks, but needs retry
     logic under contention, which is common on hot accounts.
  2. Pessimistic row locks on both accounts - simple, correct, predictable
     latency for moderate contention.
  3. Event-sourced ledger - strongest audit story, but much higher
     complexity than this stage needs.
- Decision:
  - Every transfer requires an `Idempotency-Key`. A repeat with the same
    payload returns the original result (200); a repeat with a different
    payload is rejected (409). A unique DB constraint protects against two
    concurrent requests with the same key.
  - Debit, credit, transfer record and audit entry are written in one
    database transaction.
  - Both accounts are locked with `SELECT ... FOR UPDATE` in a fixed id
    order, so opposite concurrent transfers cannot deadlock.
  - Money uses `BigDecimal` with 2-decimal precision; amounts with more
    decimals are rejected rather than rounded.
- Consequences: Correctness first, with a clear audit trail. Throughput on
  very hot accounts is limited by lock contention; revisit with a ledger
  model or per-account sequencing if that becomes the bottleneck.
