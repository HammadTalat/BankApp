package com.redmath.bankapp.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSpent,
        long transactionCount,
        BigDecimal largestExpense
) {
}
