package com.redmath.bankapp.account.repository;

import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AccountBalanceRepositoryTest {

    private static final String ACCOUNT_NUMBER = "7200000000000001";

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldFindBalanceByAccountNumber() {
        BankAccount account = createBankAccount();
        AccountBalance savedBalance = accountBalanceRepository.saveAndFlush(
                new AccountBalance(account)
        );

        AccountBalance foundBalance = accountBalanceRepository
                .findByAccount_AccountNumber(ACCOUNT_NUMBER)
                .orElseThrow();

        assertEquals(savedBalance.getId(), foundBalance.getId());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(foundBalance.getAmount())
        );
    }

    @Test
    void shouldCheckWhetherAccountHasBalance() {
        BankAccount account = createBankAccount();
        accountBalanceRepository.saveAndFlush(new AccountBalance(account));

        boolean balanceExists = accountBalanceRepository
                .existsByAccount_AccountNumber(ACCOUNT_NUMBER);
        boolean missingBalanceExists = accountBalanceRepository
                .existsByAccount_AccountNumber("7299999999999999");

        assertTrue(balanceExists);
        assertFalse(missingBalanceExists);
    }

    @Test
    void shouldFindLatestBalance() {
        BankAccount account = createBankAccount();
        accountBalanceRepository.saveAndFlush(
                new AccountBalance(
                        account,
                        new BigDecimal("100.00"),
                        BalanceIndicator.CREDIT
                )
        );
        AccountBalance latestBalance = accountBalanceRepository.saveAndFlush(
                new AccountBalance(
                        account,
                        new BigDecimal("75.00"),
                        BalanceIndicator.DEBIT
                )
        );

        AccountBalance foundBalance = accountBalanceRepository
                .findLatestBalance(ACCOUNT_NUMBER)
                .orElseThrow();

        assertEquals(latestBalance.getId(), foundBalance.getId());
        assertEquals(
                0,
                new BigDecimal("75.00").compareTo(foundBalance.getAmount())
        );
        assertEquals(BalanceIndicator.DEBIT, foundBalance.getIndicator());
    }

    @Test
    void shouldFindLatestBalanceForUpdate() {
        BankAccount account = createBankAccount();
        accountBalanceRepository.saveAndFlush(
                new AccountBalance(
                        account,
                        new BigDecimal("500.00"),
                        BalanceIndicator.CREDIT
                )
        );
        AccountBalance latestBalance = accountBalanceRepository.saveAndFlush(
                new AccountBalance(
                        account,
                        new BigDecimal("450.00"),
                        BalanceIndicator.DEBIT
                )
        );

        AccountBalance foundBalance = accountBalanceRepository
                .findLatestBalanceForUpdate(ACCOUNT_NUMBER)
                .orElseThrow();

        assertEquals(latestBalance.getId(), foundBalance.getId());
        assertEquals(
                0,
                new BigDecimal("450.00").compareTo(foundBalance.getAmount())
        );
    }

    @Test
    void shouldReturnEmptyWhenBalanceDoesNotExist() {
        assertTrue(accountBalanceRepository
                .findLatestBalance(
                        "7299999999999999"
                )
                .isEmpty());
    }

    private BankAccount createBankAccount() {
        AppUser user = new AppUser(
                null,
                "Balance Repository User",
                "balance.repository@example.com",
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
        AppUser savedUser = appUserRepository.saveAndFlush(user);

        BankAccount account = new BankAccount(ACCOUNT_NUMBER, savedUser);
        return bankAccountRepository.saveAndFlush(account);
    }
}
