package com.redmath.bankapp.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String operationId,
        OperationStatus status,
        BigDecimal amount,
        String senderAccountNumber,
        String receiverAccountNumber,
        String description,
        LocalDateTime timestamp
) {
    /**
     * Helper factory method to create a successful TransferResponse
     */
    public static TransferResponse success(
            String operationId,
            BigDecimal amount,
            String senderAccountNumber,
            String receiverAccountNumber,
            String description,
            LocalDateTime timestamp) {
        return new TransferResponse(
                operationId,
                OperationStatus.COMPLETED,
                amount,
                senderAccountNumber,
                receiverAccountNumber,
                description,
                timestamp != null ? timestamp : LocalDateTime.now()
        );
    }
}