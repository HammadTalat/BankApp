package com.redmath.bankapp.account.dto;

import com.redmath.bankapp.account.entity.AccountStatus;

public record AccountResponse(
        String accountNumber,
        AccountStatus status
) {
}
