package com.redmath.bankapp.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.AccountClosureResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountDetailsResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountSummaryResponse;
import com.redmath.bankapp.admin.exception.InvalidAccountStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    private static final Long USER_ID = 1L;
    private static final String ACCOUNT_NUMBER = "5839201746382915";
    private static final String EMAIL = "ali@example.com";

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @InjectMocks
    private AdminAccountService adminAccountService;

    @Test
    void shouldListAllAccountsWhenSearchAndStatusAreMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        BankAccount account = createBankAccount();
        AccountBalance balance = createBalance(account, "1250.50");
        when(bankAccountRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        Page<AdminAccountSummaryResponse> result =
                adminAccountService.getAccounts(null, null, pageable);

        AdminAccountSummaryResponse response = result.getContent().get(0);
        assertEquals(1, result.getTotalElements());
        assertEquals(ACCOUNT_NUMBER, response.accountNumber());
        assertEquals(AccountStatus.ACTIVE, response.accountStatus());
        assertEquals(USER_ID, response.userId());
        assertEquals("Ali Khan", response.holderName());
        assertEquals(EMAIL, response.holderEmail());
        assertEquals(0, response.balance().compareTo(new BigDecimal("1250.50")));
        verify(bankAccountRepository).findAll(pageable);
    }

    @Test
    void shouldReturnClosedAccountsWhenStatusIsClosed() {
        Pageable pageable = PageRequest.of(0, 10);
        BankAccount account = createBankAccount();
        account.close();
        AccountBalance balance = createBalance(account, "0.00");
        when(bankAccountRepository.findAllByStatus(
                AccountStatus.CLOSED,
                pageable
        )).thenReturn(new PageImpl<>(List.of(account)));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        Page<AdminAccountSummaryResponse> result =
                adminAccountService.getAccounts(
                        null,
                        AccountStatus.CLOSED,
                        pageable
                );

        assertEquals(AccountStatus.CLOSED,
                result.getContent().get(0).accountStatus());
        verify(bankAccountRepository).findAllByStatus(
                AccountStatus.CLOSED,
                pageable
        );
    }

    @Test
    void shouldSearchAccountsWithoutStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        BankAccount account = createBankAccount();
        AccountBalance balance = createBalance(account, "50.00");
        when(bankAccountRepository.searchAccounts("Ali", null, pageable))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        Page<AdminAccountSummaryResponse> result =
                adminAccountService.getAccounts("  Ali  ", null, pageable);

        assertFalse(result.isEmpty());
        assertEquals("Ali Khan", result.getContent().get(0).holderName());
        verify(bankAccountRepository).searchAccounts("Ali", null, pageable);
    }

    @Test
    void shouldSearchActiveAccounts() {
        Pageable pageable = PageRequest.of(1, 5);
        BankAccount account = createBankAccount();
        AccountBalance balance = createBalance(account, "90.00");
        when(bankAccountRepository.searchAccounts(
                "example.com",
                AccountStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(account)));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        Page<AdminAccountSummaryResponse> result =
                adminAccountService.getAccounts(
                        "example.com",
                        AccountStatus.ACTIVE,
                        pageable
                );

        assertEquals(EMAIL, result.getContent().get(0).holderEmail());
        verify(bankAccountRepository).searchAccounts(
                "example.com",
                AccountStatus.ACTIVE,
                pageable
        );
    }

    @Test
    void shouldTreatBlankSearchLikeNoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bankAccountRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<AdminAccountSummaryResponse> result =
                adminAccountService.getAccounts("   ", null, pageable);

        assertTrue(result.isEmpty());
        verify(bankAccountRepository).findAll(pageable);
        verify(bankAccountRepository, never())
                .searchAccounts("   ", null, pageable);
    }

    @Test
    void shouldReturnAccountDetails() {
        BankAccount account = createBankAccount();
        AccountBalance balance = new AccountBalance(
                account,
                new BigDecimal("875.25"),
                BalanceIndicator.CREDIT
        );
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        AdminAccountDetailsResponse result =
                adminAccountService.getAccountDetails(ACCOUNT_NUMBER);

        assertEquals(ACCOUNT_NUMBER, result.accountNumber());
        assertEquals(AccountStatus.ACTIVE, result.accountStatus());
        assertEquals(USER_ID, result.userId());
        assertEquals("Ali Khan", result.holderName());
        assertEquals(EMAIL, result.holderEmail());
        assertEquals("Lahore", result.holderAddress());
        assertEquals(ApprovalStatus.APPROVED, result.approvalStatus());
        assertEquals(0, result.balance().compareTo(new BigDecimal("875.25")));
        assertEquals(BalanceIndicator.CREDIT, result.balanceIndicator());
        assertEquals(balance.getBalanceDate(), result.balanceDate());
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() {
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminAccountService.getAccountDetails(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Bank account not found: " + ACCOUNT_NUMBER,
                exception.getMessage()
        );
        verifyNoInteractions(accountBalanceRepository);
    }

    @Test
    void shouldReturnNotFoundWhenAccountBalanceDoesNotExist() {
        BankAccount account = createBankAccount();
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminAccountService.getAccountDetails(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Balance not found for account: " + ACCOUNT_NUMBER,
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnNotFoundWhenListedAccountHasNoBalance() {
        Pageable pageable = PageRequest.of(0, 10);
        BankAccount account = createBankAccount();
        when(bankAccountRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminAccountService.getAccounts(null, null, pageable)
        );

        assertEquals(
                "Balance not found for account: " + ACCOUNT_NUMBER,
                exception.getMessage()
        );
    }

    @Test
    void shouldCloseAccountWhenBalanceIsZero() {
        BankAccount account = createBankAccount();
        AccountBalance balance = createBalance(account, "0.00");
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));
        when(bankAccountRepository.save(account)).thenReturn(account);

        AccountClosureResponse result =
                adminAccountService.closeAccount(ACCOUNT_NUMBER);

        assertTrue(account.isClosed());
        assertEquals(ACCOUNT_NUMBER, result.accountNumber());
        assertEquals(AccountStatus.CLOSED, result.accountStatus());
        assertEquals(0, result.finalBalance().compareTo(BigDecimal.ZERO));
        verify(bankAccountRepository).save(account);
    }

    @Test
    void shouldNotCloseAccountWhenBalanceIsNotZero() {
        BankAccount account = createBankAccount();
        AccountBalance balance = createBalance(account, "10.00");
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.of(balance));

        InvalidAccountStateException exception = assertThrows(
                InvalidAccountStateException.class,
                () -> adminAccountService.closeAccount(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Account balance must be zero before closing",
                exception.getMessage()
        );
        assertTrue(account.isActive());
        verify(bankAccountRepository, never()).save(account);
    }

    @Test
    void shouldNotCloseAccountWhenItIsAlreadyClosed() {
        BankAccount account = createBankAccount();
        account.close();
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));

        InvalidAccountStateException exception = assertThrows(
                InvalidAccountStateException.class,
                () -> adminAccountService.closeAccount(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Only active accounts can be closed",
                exception.getMessage()
        );
        verifyNoInteractions(accountBalanceRepository);
        verify(bankAccountRepository, never()).save(account);
    }

    @Test
    void shouldReturnNotFoundWhenClosingAccountDoesNotExist() {
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminAccountService.closeAccount(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Bank account not found: " + ACCOUNT_NUMBER,
                exception.getMessage()
        );
        verifyNoInteractions(accountBalanceRepository);
    }

    @Test
    void shouldReturnNotFoundWhenClosingAccountHasNoBalance() {
        BankAccount account = createBankAccount();
        when(bankAccountRepository.findById(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(accountBalanceRepository
                .findLatestBalance(
                        ACCOUNT_NUMBER
                ))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminAccountService.closeAccount(ACCOUNT_NUMBER)
        );

        assertEquals(
                "Balance not found for account: " + ACCOUNT_NUMBER,
                exception.getMessage()
        );
        assertTrue(account.isActive());
        verify(bankAccountRepository, never()).save(account);
    }

    private AppUser createUser() {
        return new AppUser(
                USER_ID,
                "Ali Khan",
                EMAIL,
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
    }

    private BankAccount createBankAccount() {
        return new BankAccount(ACCOUNT_NUMBER, createUser());
    }

    private AccountBalance createBalance(
            BankAccount account,
            String amount
    ) {
        BigDecimal balanceAmount = new BigDecimal(amount);
        BalanceIndicator indicator = balanceAmount.compareTo(BigDecimal.ZERO) == 0
                ? BalanceIndicator.NONE
                : BalanceIndicator.CREDIT;

        return new AccountBalance(
                account,
                balanceAmount,
                indicator
        );
    }
}
