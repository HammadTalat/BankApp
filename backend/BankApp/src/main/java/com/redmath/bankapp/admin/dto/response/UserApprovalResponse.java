package com.redmath.bankapp.admin.dto.response;


import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.user.entity.ApprovalStatus;

import java.math.BigDecimal;

public record UserApprovalResponse(
        Long userId,
        String name,
        String email,
        ApprovalStatus approvalStatus,
        String accountNumber,
        AccountStatus accountStatus,
        BigDecimal balance
) {
}