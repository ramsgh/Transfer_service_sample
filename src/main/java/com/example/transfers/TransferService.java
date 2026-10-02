package com.example.transfers;

import com.example.transfers.TransferDtos.TransferRequest;
import com.example.transfers.TransferDtos.TransferResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moves money between two accounts atomically and idempotently.
 * See docs/adr/0001-idempotent-transfers.md for the reasoning.
 */
@Service
public class TransferService {

    public record TransferResult(TransferResponse transfer, boolean created) { }

    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final AuditRepository audit;

    public TransferService(AccountRepository accounts, TransferRepository transfers, AuditRepository audit) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.audit = audit;
    }

    @Transactional
    public TransferResult transfer(String idempotencyKey, TransferRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidTransferException("Idempotency-Key header is required");
        }

        // 1. Replay of a known request returns the original result; nothing is moved twice.
        Optional<Transfer> existing = transfers.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Transfer t = existing.get();
            if (!t.matches(request)) {
                throw new IdempotencyConflictException(idempotencyKey);
            }
            return new TransferResult(TransferResponse.from(t), false);
        }

        // 2. Business validation.
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidTransferException("Source and destination accounts must differ");
        }
        if (request.amount().scale() > 2) {
            throw new InvalidTransferException("Amount supports at most 2 decimal places");
        }
        BigDecimal amount = request.amount().setScale(2);
        String currency = request.currency().toUpperCase();

        // 3. Lock both accounts in a fixed (id) order so concurrent opposite transfers cannot deadlock.
        UUID first = request.fromAccountId().compareTo(request.toAccountId()) < 0
                ? request.fromAccountId() : request.toAccountId();
        UUID second = first.equals(request.fromAccountId())
                ? request.toAccountId() : request.fromAccountId();
        Account firstAccount = lock(first);
        Account secondAccount = lock(second);
        Account from = first.equals(request.fromAccountId()) ? firstAccount : secondAccount;
        Account to = from == firstAccount ? secondAccount : firstAccount;

        if (!from.getCurrency().equals(currency) || !to.getCurrency().equals(currency)) {
            throw new InvalidTransferException("Currency mismatch between request and accounts");
        }

        // 4. Move the money, record the transfer and the audit entry in ONE transaction.
        from.debit(amount);
        to.credit(amount);

        Transfer saved = transfers.save(new Transfer(
                idempotencyKey, from.getId(), to.getId(), amount, currency));
        audit.save(new AuditEntry(saved.getId(), "TRANSFER_COMPLETED",
                "from=" + from.getId() + " to=" + to.getId() + " amount=" + amount + " " + currency));

        return new TransferResult(TransferResponse.from(saved), true);
    }

    @Transactional(readOnly = true)
    public TransferResponse get(UUID id) {
        return transfers.findById(id)
                .map(TransferResponse::from)
                .orElseThrow(() -> new TransferNotFoundException(id));
    }

    private Account lock(UUID id) {
        return accounts.findForUpdate(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
