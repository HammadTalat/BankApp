package com.redmath.bankapp.account.repository;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankAccountRepositoryTest {

    private static final String ACTIVE_ACCOUNT_NUMBER = "7100000000000001";
    private static final String CLOSED_ACCOUNT_NUMBER = "7100000000000002";

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldFindAccountByUserId() {
        BankAccount savedAccount = createAccount(
                "Ali Repository",
                "ali.repository@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        BankAccount foundAccount = bankAccountRepository
                .findByUser_Id(savedAccount.getUser().getId())
                .orElseThrow();

        assertEquals(ACTIVE_ACCOUNT_NUMBER, foundAccount.getAccountNumber());
    }

    @Test
    void shouldCheckWhetherUserHasAccount() {
        AppUser userWithAccount = createAccount(
                "Ayesha Repository",
                "ayesha.repository@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        ).getUser();
        AppUser userWithoutAccount = saveUser(
                "Bilal Repository",
                "bilal.repository@example.com"
        );

        boolean accountExists = bankAccountRepository
                .existsByUser_Id(userWithAccount.getId());
        boolean accountDoesNotExist = bankAccountRepository
                .existsByUser_Id(userWithoutAccount.getId());

        assertTrue(accountExists);
        assertFalse(accountDoesNotExist);
    }

    @Test
    void shouldReturnOnlyActiveAccounts() {
        createAccount(
                "Active Repository User",
                "active.repository@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );
        createAccount(
                "Closed Repository User",
                "closed.repository@example.com",
                CLOSED_ACCOUNT_NUMBER,
                AccountStatus.CLOSED
        );

        Page<BankAccount> result = bankAccountRepository.findAllByStatus(
                AccountStatus.ACTIVE,
                PageRequest.of(0, 100)
        );

        assertTrue(containsAccount(result, ACTIVE_ACCOUNT_NUMBER));
        assertFalse(containsAccount(result, CLOSED_ACCOUNT_NUMBER));
        assertTrue(result.getContent().stream().allMatch(BankAccount::isActive));
    }

    @Test
    void shouldCountAccountsByStatus() {
        long activeCountBefore = bankAccountRepository
                .countByStatus(AccountStatus.ACTIVE);
        createAccount(
                "Count Repository User",
                "count.repository@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        long activeCountAfter = bankAccountRepository
                .countByStatus(AccountStatus.ACTIVE);

        assertEquals(activeCountBefore + 1, activeCountAfter);
    }

    @Test
    void shouldSearchAccountByAccountNumber() {
        createAccount(
                "Number Search User",
                "number.search@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        Page<BankAccount> result = bankAccountRepository.searchAccounts(
                ACTIVE_ACCOUNT_NUMBER,
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                ACTIVE_ACCOUNT_NUMBER,
                result.getContent().get(0).getAccountNumber()
        );
    }

    @Test
    void shouldSearchAccountByUserName() {
        createAccount(
                "Unique Repository Customer",
                "name.search@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        Page<BankAccount> result = bankAccountRepository.searchAccounts(
                "unique repository customer",
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Unique Repository Customer",
                result.getContent().get(0).getUser().getName()
        );
    }

    @Test
    void shouldSearchAccountByUserEmail() {
        createAccount(
                "Email Search User",
                "unique.email.search@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        Page<BankAccount> result = bankAccountRepository.searchAccounts(
                "UNIQUE.EMAIL.SEARCH@EXAMPLE.COM",
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "unique.email.search@example.com",
                result.getContent().get(0).getUser().getEmail()
        );
    }

    @Test
    void shouldSearchAccountsWithStatusFilter() {
        createAccount(
                "Active Filter User",
                "active.repository-filter@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );
        createAccount(
                "Closed Filter User",
                "closed.repository-filter@example.com",
                CLOSED_ACCOUNT_NUMBER,
                AccountStatus.CLOSED
        );

        Page<BankAccount> result = bankAccountRepository.searchAccounts(
                "repository-filter",
                AccountStatus.CLOSED,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                CLOSED_ACCOUNT_NUMBER,
                result.getContent().get(0).getAccountNumber()
        );
        assertTrue(result.getContent().get(0).isClosed());
    }

    @Test
    void shouldReturnAccountsWhenSearchAndStatusAreMissing() {
        createAccount(
                "No Filter User",
                "no.filter@example.com",
                ACTIVE_ACCOUNT_NUMBER,
                AccountStatus.ACTIVE
        );

        Page<BankAccount> result = bankAccountRepository.searchAccounts(
                null,
                null,
                PageRequest.of(0, 100)
        );

        assertTrue(containsAccount(result, ACTIVE_ACCOUNT_NUMBER));
    }

    private BankAccount createAccount(
            String name,
            String email,
            String accountNumber,
            AccountStatus status
    ) {
        AppUser user = saveUser(name, email);
        BankAccount account = new BankAccount(accountNumber, user);
        account.setStatus(status);
        return bankAccountRepository.saveAndFlush(account);
    }

    private AppUser saveUser(String name, String email) {
        AppUser user = new AppUser(
                null,
                name,
                email,
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.APPROVED
        );
        return appUserRepository.saveAndFlush(user);
    }

    private boolean containsAccount(
            Page<BankAccount> accounts,
            String accountNumber
    ) {
        return accounts.getContent().stream()
                .anyMatch(account -> accountNumber.equals(account.getAccountNumber()));
    }
}
