package com.redmath.transactionverification.dto;

public record TransactionEvaluationResponse(
        boolean allowed,
        String status,
        String message
) {}