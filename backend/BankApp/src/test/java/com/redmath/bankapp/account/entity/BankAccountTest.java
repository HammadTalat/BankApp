package com.redmath.bankapp.account.entity;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankAccountTest {

    private static final String ACCOUNT_NUMBER = "5839201746382915";

    @Test
    void shouldStartNewAccountAsActive() {
        AppUser user = createUser();

        BankAccount account = new BankAccount(ACCOUNT_NUMBER, user);

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.isActive());
        assertFalse(account.isClosed());
    }

    @Test
    void shouldStoreAccountNumberAndUser() {
        AppUser user = createUser();

        BankAccount account = new BankAccount(ACCOUNT_NUMBER, user);

        assertEquals(ACCOUNT_NUMBER, account.getAccountNumber());
        assertSame(user, account.getUser());
    }

    @Test
    void shouldCloseAccount() {
        BankAccount account = new BankAccount(ACCOUNT_NUMBER, createUser());

        account.close();

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        assertTrue(account.isClosed());
        assertFalse(account.isActive());
    }

    @Test
    void shouldRequireAccountNumber() {
        AppUser user = createUser();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BankAccount(null, user)
        );

        assertEquals("Account number is required", exception.getMessage());
    }

    @Test
    void shouldRequireUser() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BankAccount(ACCOUNT_NUMBER, null)
        );

        assertEquals("Account holder is required", exception.getMessage());
    }

    private AppUser createUser() {
        return new AppUser(
                1L,
                "Ali Khan",
                "ali@example.com",
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
    }
}
