package com.redmath.bankapp.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", length = 36, nullable = false)
    private String operationId;

    // Foreign Key: fk_transaction_account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private BankAccount account;

    // Foreign Key: fk_transaction_recipient (Nullable for deposits/withdrawals)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_account_id", referencedColumnName = "id")
    private BankAccount recipientAccount;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "indicator", length = 10, nullable = false)
    private BalanceIndicator indicator;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }
}