package com.redmath.bankapp.admin.dto.response;


public record AdminDashboardResponse(
    long pendingUsers,
    long approvedUsers,
    long rejectedUsers,
    long totalAccounts,
    long activeAccounts,
    long closedAccounts
) {

}