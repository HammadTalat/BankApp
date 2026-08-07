package com.redmath.bankapp.admin.dto.response;


import com.redmath.bankapp.account.entity.AccountStatus;
import java.math.BigDecimal;

public record AdminAccountSummaryResponse(
    String accountNumber,
    AccountStatus accountStatus,
    Long userId,
    String holderName,
    String holderEmail,
    BigDecimal balance
) {

}