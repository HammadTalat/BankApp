package com.redmath.bankapp.riskservice.dto;

public record EvaluateRiskResponse(
        boolean allowed,
        String status,
        String message
) {}