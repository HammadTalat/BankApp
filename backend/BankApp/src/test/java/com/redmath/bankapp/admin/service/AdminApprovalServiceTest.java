package com.redmath.bankapp.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.UserApprovalResponse;
import com.redmath.bankapp.admin.exception.InvalidUserStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminApprovalServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "ali@example.com";

    private AppUserRepository appUserRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountBalanceRepository accountBalanceRepository;
    private AdminApprovalService adminApprovalService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        bankAccountRepository = mock(BankAccountRepository.class);
        accountBalanceRepository = mock(AccountBalanceRepository.class);
        adminApprovalService = new AdminApprovalService(
                appUserRepository,
                bankAccountRepository,
                accountBalanceRepository
        );
    }

    @Test
    void shouldApprovePendingUserAndCreateAccountWithInitialBalance() {
        AppUser user = createPendingAccountHolder();
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(bankAccountRepository.existsByUser_Id(USER_ID))
                .thenReturn(false);
        when(bankAccountRepository.existsById(anyString()))
                .thenReturn(false);

        UserApprovalResponse result = adminApprovalService.approveUser(USER_ID);

        ArgumentCaptor<BankAccount> accountCaptor =
                ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<AccountBalance> balanceCaptor =
                ArgumentCaptor.forClass(AccountBalance.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        verify(accountBalanceRepository).save(balanceCaptor.capture());

        BankAccount savedAccount = accountCaptor.getValue();
        AccountBalance savedBalance = balanceCaptor.getValue();

        assertEquals(ApprovalStatus.APPROVED, user.getApprovalStatus());
        assertEquals(16, savedAccount.getAccountNumber().length());
        assertFalse(savedAccount.getAccountNumber().isBlank());
        assertSame(user, savedAccount.getUser());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertSame(savedAccount, savedBalance.getAccount());
        assertEquals(0, savedBalance.getAmount().compareTo(BigDecimal.ZERO));
        assertEquals(BalanceIndicator.NONE, savedBalance.getIndicator());

        assertEquals(USER_ID, result.userId());
        assertEquals("Ali Khan", result.name());
        assertEquals(EMAIL, result.email());
        assertEquals(ApprovalStatus.APPROVED, result.approvalStatus());
        assertEquals(savedAccount.getAccountNumber(), result.accountNumber());
        assertEquals(AccountStatus.ACTIVE, result.accountStatus());
        assertEquals(0, result.balance().compareTo(BigDecimal.ZERO));
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldTryAgainWhenGeneratedAccountNumberAlreadyExists() {
        AppUser user = createPendingAccountHolder();
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(bankAccountRepository.existsByUser_Id(USER_ID))
                .thenReturn(false);
        when(bankAccountRepository.existsById(anyString()))
                .thenReturn(true, false);

        UserApprovalResponse result = adminApprovalService.approveUser(USER_ID);

        assertEquals(16, result.accountNumber().length());
        verify(bankAccountRepository, times(2)).existsById(anyString());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
        verify(accountBalanceRepository, never())
                .save(any(AccountBalance.class));
    }

    @Test
    void shouldReturnErrorWhenUserIsAlreadyApproved() {
        AppUser user = createPendingAccountHolder();
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals(
                "Only pending users can be approved",
                exception.getMessage()
        );
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldReturnErrorWhenUserIsAlreadyRejected() {
        AppUser user = createPendingAccountHolder();
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals(
                "Only pending users can be approved",
                exception.getMessage()
        );
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldReturnErrorWhenUserAlreadyHasBankAccount() {
        AppUser user = createPendingAccountHolder();
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(bankAccountRepository.existsByUser_Id(USER_ID))
                .thenReturn(true);

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals(
                "The user already has a bank account",
                exception.getMessage()
        );
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
        verify(accountBalanceRepository, never())
                .save(any(AccountBalance.class));
    }

    @Test
    void shouldReturnErrorWhenUserIsNotAccountHolder() {
        AppUser admin = new AppUser(
                USER_ID,
                "Bank Admin",
                "admin@example.com",
                "Head Office",
                Role.ADMIN,
                ApprovalStatus.PENDING
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(admin));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals(
                "Only account holders can be approved",
                exception.getMessage()
        );
        verify(bankAccountRepository, never())
                .existsByUser_Id(USER_ID);
        verify(appUserRepository, never()).save(admin);
    }

    @Test
    void shouldReturnErrorWhenUserAddressIsBlank() {
        AppUser user = createPendingAccountHolder();
        user.setAddress("   ");
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminApprovalService.approveUser(USER_ID)
        );

        assertEquals(
                "User address is required before approval",
                exception.getMessage()
        );
        verify(bankAccountRepository, never())
                .existsByUser_Id(USER_ID);
        verify(appUserRepository, never()).save(user);
    }

    private AppUser createPendingAccountHolder() {
        return new AppUser(
                USER_ID,
                "Ali Khan",
                EMAIL,
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.PENDING
        );
    }
}
