package com.example.transfers;

import com.example.transfers.TransferDtos.AccountResponse;
import com.example.transfers.TransferDtos.CreateAccountRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        Account saved = accounts.save(new Account(
                request.owner(),
                request.currency().toUpperCase(),
                request.initialBalance().setScale(2, java.math.RoundingMode.UNNECESSARY)));
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID id) {
        return accounts.findById(id)
                .map(AccountResponse::from)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
