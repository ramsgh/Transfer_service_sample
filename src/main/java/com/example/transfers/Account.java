package com.example.transfers;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected Account() { }

    public Account(String owner, String currency, BigDecimal initialBalance) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.currency = currency;
        this.balance = initialBalance;
    }

    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id);
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public UUID getId() { return id; }
    public String getOwner() { return owner; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
}
