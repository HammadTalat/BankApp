package com.redmath.bankapp.transaction.dto;
import com.redmath.bankapp.transaction.entity.AccountTransaction;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String operationId,
        String description,
        BigDecimal amount,
        TransactionIndicator indicator,
        LocalDateTime transactionDate,
        String accountId,          // Primary account number
        String recipientAccountId  // Recipient account number (null for non-transfer ops)
) {
    /**
     * Factory method to map from AccountTransaction entity to DTO
     */
    public static TransactionResponse fromEntity(AccountTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getOperationId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getIndicator(),
                transaction.getTransactionDate(),
                transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null,
                transaction.getRecipientAccount() != null ? transaction.getRecipientAccount().getAccountNumber() : null
        );
    }
}