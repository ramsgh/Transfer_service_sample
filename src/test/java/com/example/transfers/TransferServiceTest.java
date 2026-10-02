package com.example.transfers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.transfers.TransferDtos.AccountResponse;
import com.example.transfers.TransferDtos.CreateAccountRequest;
import com.example.transfers.TransferDtos.TransferRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransferServiceTest {

    @Autowired AccountService accounts;
    @Autowired TransferService transfers;

    private AccountResponse account(String owner, String balance) {
        return accounts.create(new CreateAccountRequest(owner, "EUR", new BigDecimal(balance)));
    }

    private void assertBalance(AccountResponse a, String expected) {
        assertEquals(0, new BigDecimal(expected).compareTo(accounts.get(a.id()).balance()));
    }

    @Test
    void movesMoneyAndReplayDoesNotDoubleDebit() {
        AccountResponse alice = account("alice", "100.00");
        AccountResponse bob = account("bob", "0.00");
        TransferRequest request = new TransferRequest(alice.id(), bob.id(), new BigDecimal("25.00"), "EUR");

        TransferService.TransferResult first = transfers.transfer("key-1", request);
        TransferService.TransferResult replay = transfers.transfer("key-1", request);

        assertTrue(first.created());
        assertFalse(replay.created());
        assertEquals(first.transfer().id(), replay.transfer().id());
        assertBalance(alice, "75.00");
        assertBalance(bob, "25.00");
    }

    @Test
    void rejectsInsufficientFundsAndLeavesBalancesUntouched() {
        AccountResponse alice = account("alice", "10.00");
        AccountResponse bob = account("bob", "0.00");
        TransferRequest request = new TransferRequest(alice.id(), bob.id(), new BigDecimal("50.00"), "EUR");

        assertThrows(InsufficientFundsException.class, () -> transfers.transfer("key-2", request));

        assertBalance(alice, "10.00");
        assertBalance(bob, "0.00");
    }

    @Test
    void sameKeyWithDifferentPayloadIsAConflict() {
        AccountResponse alice = account("alice", "100.00");
        AccountResponse bob = account("bob", "0.00");

        transfers.transfer("key-3", new TransferRequest(alice.id(), bob.id(), new BigDecimal("10.00"), "EUR"));

        assertThrows(IdempotencyConflictException.class, () ->
                transfers.transfer("key-3",
                        new TransferRequest(alice.id(), bob.id(), new BigDecimal("20.00"), "EUR")));
    }

    @Test
    void rejectsTransferToSameAccount() {
        AccountResponse alice = account("alice", "100.00");

        assertThrows(InvalidTransferException.class, () ->
                transfers.transfer("key-4",
                        new TransferRequest(alice.id(), alice.id(), new BigDecimal("5.00"), "EUR")));
    }
}
