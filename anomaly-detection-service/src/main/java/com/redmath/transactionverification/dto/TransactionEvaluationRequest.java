package com.redmath.transactionverification.dto;

import java.util.List;

public record TransactionEvaluationRequest (
        TransactionDetail currentTransaction,
        List<TransactionDetail> previousHistory
) {}