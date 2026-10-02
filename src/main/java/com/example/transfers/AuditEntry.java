package com.example.transfers;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Append-only audit trail. Written in the same transaction as the money movement. */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID transferId;

    @Column(nullable = false)
    private String eventType;

    @Column(length = 500)
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEntry() { }

    public AuditEntry(UUID transferId, String eventType, String details) {
        this.id = UUID.randomUUID();
        this.transferId = transferId;
        this.eventType = eventType;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public UUID getTransferId() { return transferId; }
    public String getEventType() { return eventType; }
}
