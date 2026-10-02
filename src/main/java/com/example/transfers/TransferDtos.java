package com.example.transfers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransferDtos {
    private TransferDtos() { }

    public record CreateAccountRequest(
            @NotBlank String owner,
            @NotBlank @Size(min = 3, max = 3) String currency,
            @NotNull @PositiveOrZero BigDecimal initialBalance) { }

    public record AccountResponse(UUID id, String owner, String currency, BigDecimal balance) {
        static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getOwner(), a.getCurrency(), a.getBalance());
        }
    }

    public record TransferRequest(
            @NotNull UUID fromAccountId,
            @NotNull UUID toAccountId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency) { }

    public record TransferResponse(
            UUID id, UUID fromAccountId, UUID toAccountId,
            BigDecimal amount, String currency, TransferStatus status, Instant createdAt) {
        static TransferResponse from(Transfer t) {
            return new TransferResponse(t.getId(), t.getFromAccountId(), t.getToAccountId(),
                    t.getAmount(), t.getCurrency(), t.getStatus(), t.getCreatedAt());
        }
    }
}
