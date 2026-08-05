package com.redmath.bankapp.account.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal amount
) {
}
