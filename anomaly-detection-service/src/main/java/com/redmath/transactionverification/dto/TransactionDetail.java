package com.redmath.transactionverification.dto;

public record TransactionDetail(
        String transactionId,
        double amount,
        String timestamp
) {}