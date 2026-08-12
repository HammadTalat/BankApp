package com.redmath.redmathbankmcp.dto;

import java.math.BigDecimal;

public record AccountSummaryResponse(
        String accountNumber,
        String status,
        BigDecimal balance
) {
}
