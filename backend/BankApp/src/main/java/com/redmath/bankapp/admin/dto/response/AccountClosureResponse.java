package com.redmath.bankapp.admin.dto.response;


import com.redmath.bankapp.account.entity.AccountStatus;
import java.math.BigDecimal;

public record AccountClosureResponse(
    String accountNumber,
    AccountStatus accountStatus,
    BigDecimal finalBalance
) {

}