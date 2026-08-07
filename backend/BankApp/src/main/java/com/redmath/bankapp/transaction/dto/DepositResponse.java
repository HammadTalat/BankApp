package com.redmath.bankapp.transaction.dto;
import com.redmath.bankapp.transaction.enums.OperationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositResponse(
        String operationId,
        OperationStatus status,
        BigDecimal amount,
        String accountNumber,
        BigDecimal newBalance,
        String description,
        LocalDateTime transactionDate
) {}