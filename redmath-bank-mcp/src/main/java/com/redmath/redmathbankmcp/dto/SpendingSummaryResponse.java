package com.redmath.redmathbankmcp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpendingSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSpent,
        long transactionCount,
        BigDecimal largestExpense
) {
}
