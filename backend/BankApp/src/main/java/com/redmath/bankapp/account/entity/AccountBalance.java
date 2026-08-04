package com.redmath.bankapp.account.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "account_balance")
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "account_number",
            referencedColumnName = "account_number",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_account_balance_account"
            )
    )
    private BankAccount account;

    @Column(
            name = "balance_date",
            nullable = false
    )
    private LocalDateTime balanceDate;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "indicator",
            nullable = false,
            length = 10
    )
    private BalanceIndicator indicator;

    public AccountBalance() {
    }

    public AccountBalance(BankAccount account) {
        this.account = Objects.requireNonNull(
                account,
                "Bank account is required"
        );

        this.amount = BigDecimal.ZERO;
        this.indicator = BalanceIndicator.NONE;
        this.balanceDate = LocalDateTime.now();
    }

    public AccountBalance(BankAccount account, BigDecimal amount, BalanceIndicator indicator) {
        this.account = account;
        this.amount = amount;
        this.indicator = indicator;
        this.balanceDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BankAccount getAccount() {
        return account;
    }

    public LocalDateTime getBalanceDate() {
        return balanceDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BalanceIndicator getIndicator() {
        return indicator;
    }

    public void updateBalance(
            BigDecimal newAmount,
            BalanceIndicator newIndicator
    ) {
        Objects.requireNonNull(
                newAmount,
                "Balance amount is required"
        );

        Objects.requireNonNull(
                newIndicator,
                "Balance indicator is required"
        );

        if (newAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Account balance cannot be negative"
            );
        }

        if (newIndicator == BalanceIndicator.NONE) {
            throw new IllegalArgumentException(
                    "NONE cannot be used for a balance update"
            );
        }

        this.amount = newAmount;
        this.indicator = newIndicator;
        this.balanceDate = LocalDateTime.now();
    }
}