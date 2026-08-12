package com.redmath.bankapp.transaction.repository;

import java.math.BigDecimal;

/**
 * Database aggregate values for debit transactions. Monetary values can be null when no
 * matching transactions exist and are normalized by the service response.
 */
public record SpendingAggregate(
        BigDecimal totalSpent,
        long transactionCount,
        BigDecimal largestExpense
) {
}
