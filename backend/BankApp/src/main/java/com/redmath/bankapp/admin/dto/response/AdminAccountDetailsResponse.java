package com.redmath.bankapp.admin.dto.response;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAccountDetailsResponse(
        String accountNumber,
        AccountStatus accountStatus,
        Long userId,
        String holderName,
        String holderEmail,
        String holderAddress,
        ApprovalStatus approvalStatus,
        BigDecimal balance,
        BalanceIndicator balanceIndicator,
        LocalDateTime balanceDate
) {
}