package com.redmath.redmathbankmcp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionResponse(

        Long id,

        String operationId,

        String description,

        BigDecimal amount,

        String indicator,

        LocalDateTime transactionDate,

        String accountId,

        String recipientAccountId

) {
}
