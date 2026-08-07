package com.redmath.bankapp.transaction.entity;

import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  @JoinColumn(name = "account_number", referencedColumnName = "account_number", nullable = false)
  private BankAccount account;

  // Foreign Key: fk_transaction_recipient (Nullable for deposits/withdrawals)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_account_number", referencedColumnName = "account_number")
  private BankAccount recipientAccount;

  @Column(name = "description", length = 255, nullable = false)
  private String description;

  @Column(name = "amount", precision = 19, scale = 2, nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "indicator", length = 10, nullable = false)
  private TransactionIndicator indicator;

  @Column(name = "transaction_date", nullable = false, updatable = false)
  private LocalDateTime transactionDate;

  @PrePersist
  protected void onCreate() {
    if (this.transactionDate == null) {
      this.transactionDate = LocalDateTime.now();
    }
  }
}