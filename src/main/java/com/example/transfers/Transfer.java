package com.example.transfers;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    private UUID id;

    // Unique constraint is the last line of defence against duplicate processing.
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID fromAccountId;

    @Column(nullable = false)
    private UUID toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Transfer() { }

    public Transfer(String idempotencyKey, UUID fromAccountId, UUID toAccountId,
                    BigDecimal amount, String currency) {
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = TransferStatus.COMPLETED;
        this.createdAt = Instant.now(); // UTC
    }

    /** True if a replayed request carries the same payload as the original. */
    public boolean matches(TransferDtos.TransferRequest r) {
        return fromAccountId.equals(r.fromAccountId())
                && toAccountId.equals(r.toAccountId())
                && amount.compareTo(r.amount()) == 0
                && currency.equalsIgnoreCase(r.currency());
    }

    public UUID getId() { return id; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
