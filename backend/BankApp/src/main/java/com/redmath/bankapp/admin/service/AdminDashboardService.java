package com.redmath.bankapp.admin.service;


import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.AdminDashboardResponse;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AppUserRepository appUserRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardSummary() {
        long pendingUsers = countUsers(ApprovalStatus.PENDING);
        long approvedUsers = countUsers(ApprovalStatus.APPROVED);
        long rejectedUsers = countUsers(ApprovalStatus.REJECTED);

        long totalAccounts = bankAccountRepository.count();

        long activeAccounts =
                bankAccountRepository.countByStatus(
                        AccountStatus.ACTIVE
                );

        long closedAccounts =
                bankAccountRepository.countByStatus(
                        AccountStatus.CLOSED
                );

        return new AdminDashboardResponse(
                pendingUsers,
                approvedUsers,
                rejectedUsers,
                totalAccounts,
                activeAccounts,
                closedAccounts
        );
    }

    private long countUsers(ApprovalStatus approvalStatus) {
        return appUserRepository.countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                approvalStatus
        );
    }
}