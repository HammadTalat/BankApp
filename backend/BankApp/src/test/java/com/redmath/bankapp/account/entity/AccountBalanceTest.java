package com.redmath.bankapp.account.entity;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountBalanceTest {

    private static final String ACCOUNT_NUMBER = "5839201746382915";

    @Test
    void shouldStartWithZeroBalanceAndNoneIndicator() {
        BankAccount account = createBankAccount();

        AccountBalance balance = new AccountBalance(account);

        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getAmount()));
        assertEquals(BalanceIndicator.NONE, balance.getIndicator());
        assertSame(account, balance.getAccount());
        assertNotNull(balance.getBalanceDate());
    }

    @Test
    void shouldStoreCreditBalance() {
        BankAccount account = createBankAccount();
        BigDecimal amount = new BigDecimal("2500.00");

        AccountBalance balance = new AccountBalance(
                account,
                amount,
                BalanceIndicator.CREDIT
        );

        assertEquals(0, amount.compareTo(balance.getAmount()));
        assertEquals(BalanceIndicator.CREDIT, balance.getIndicator());
        assertSame(account, balance.getAccount());
    }

    @Test
    void shouldStoreDebitBalance() {
        BigDecimal amount = new BigDecimal("750.00");

        AccountBalance balance = new AccountBalance(
                createBankAccount(),
                amount,
                BalanceIndicator.DEBIT
        );

        assertEquals(0, amount.compareTo(balance.getAmount()));
        assertEquals(BalanceIndicator.DEBIT, balance.getIndicator());
    }

    @Test
    void shouldUpdateBalance() {
        AccountBalance balance = new AccountBalance(createBankAccount());
        BigDecimal newAmount = new BigDecimal("1200.50");

        balance.updateBalance(newAmount, BalanceIndicator.CREDIT);

        assertEquals(0, newAmount.compareTo(balance.getAmount()));
        assertEquals(BalanceIndicator.CREDIT, balance.getIndicator());
        assertNotNull(balance.getBalanceDate());
    }

    @Test
    void shouldRejectNegativeBalanceUpdate() {
        AccountBalance balance = new AccountBalance(createBankAccount());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> balance.updateBalance(
                        new BigDecimal("-1.00"),
                        BalanceIndicator.DEBIT
                )
        );

        assertEquals(
                "Account balance cannot be negative",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNoneIndicatorForBalanceUpdate() {
        AccountBalance balance = new AccountBalance(createBankAccount());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> balance.updateBalance(
                        new BigDecimal("100.00"),
                        BalanceIndicator.NONE
                )
        );

        assertEquals(
                "NONE cannot be used for a balance update",
                exception.getMessage()
        );
    }

    @Test
    void shouldRequireAmountForBalanceUpdate() {
        AccountBalance balance = new AccountBalance(createBankAccount());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> balance.updateBalance(null, BalanceIndicator.CREDIT)
        );

        assertEquals("Balance amount is required", exception.getMessage());
    }

    @Test
    void shouldRequireIndicatorForBalanceUpdate() {
        AccountBalance balance = new AccountBalance(createBankAccount());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> balance.updateBalance(BigDecimal.ZERO, null)
        );

        assertEquals("Balance indicator is required", exception.getMessage());
    }

    @Test
    void shouldRequireBankAccount() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AccountBalance(null)
        );

        assertEquals("Bank account is required", exception.getMessage());
    }

    private BankAccount createBankAccount() {
        AppUser user = new AppUser(
                1L,
                "Ali Khan",
                "ali@example.com",
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
        return new BankAccount(ACCOUNT_NUMBER, user);
    }
}
