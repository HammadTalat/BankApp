package com.redmath.bankapp.riskservice.dto;

import java.util.List;

public record EvaluateRiskRequest(
        TransactionDetail currentTransaction,
        List<TransactionDetail> previousHistory
) {}