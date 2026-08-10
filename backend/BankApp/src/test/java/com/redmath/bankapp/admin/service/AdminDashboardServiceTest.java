package com.redmath.bankapp.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.AdminDashboardResponse;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void shouldReturnDashboardCountsForAccountHolders() {
        when(appUserRepository.countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.PENDING
        )).thenReturn(3L);
        when(appUserRepository.countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        )).thenReturn(8L);
        when(appUserRepository.countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.REJECTED
        )).thenReturn(2L);
        when(bankAccountRepository.count()).thenReturn(8L);
        when(bankAccountRepository.countByStatus(AccountStatus.ACTIVE))
                .thenReturn(6L);
        when(bankAccountRepository.countByStatus(AccountStatus.CLOSED))
                .thenReturn(2L);

        AdminDashboardResponse result =
                adminDashboardService.getDashboardSummary();

        assertEquals(3L, result.pendingUsers());
        assertEquals(8L, result.approvedUsers());
        assertEquals(2L, result.rejectedUsers());
        assertEquals(8L, result.totalAccounts());
        assertEquals(6L, result.activeAccounts());
        assertEquals(2L, result.closedAccounts());
        verify(appUserRepository).countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.PENDING
        );
        verify(appUserRepository).countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
        verify(appUserRepository).countByRoleAndApprovalStatus(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.REJECTED
        );
        verify(bankAccountRepository).count();
        verify(bankAccountRepository).countByStatus(AccountStatus.ACTIVE);
        verify(bankAccountRepository).countByStatus(AccountStatus.CLOSED);
    }
}
