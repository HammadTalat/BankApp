package com.redmath.bankapp.riskservice.dto;

public record TransactionDetail(
        String transactionId,
        double amount,
        String timestamp
) {}