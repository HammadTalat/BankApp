package com.redmath.bankapp.transaction.dummyinitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.CommandLineRunner;

class DataInitializerTest {

    private final AppUserRepository userRepository = org.mockito.Mockito.mock(AppUserRepository.class);
    private final BankAccountRepository accountRepository = org.mockito.Mockito.mock(BankAccountRepository.class);
    private final AccountBalanceRepository balanceRepository = org.mockito.Mockito.mock(AccountBalanceRepository.class);
    private final DataInitializer initializer = new DataInitializer();

    @Test
    void skipsSeedingWhenTheDatabaseAlreadyContainsMoreThanOneUser() throws Exception {
        when(userRepository.count()).thenReturn(2L);

        runner().run();

        verify(userRepository, never()).save(any(AppUser.class));
        verify(accountRepository, never()).save(any(BankAccount.class));
        verify(balanceRepository, never()).save(any(AccountBalance.class));
    }

    @Test
    void seedsTwoApprovedAccountHoldersWithActiveAccountsAndOpeningBalances() throws Exception {
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        runner().run();

        ArgumentCaptor<AppUser> users = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository, times(2)).save(users.capture());
        assertThat(users.getAllValues())
                .extracting(AppUser::getEmail)
                .containsExactly("sender@redmath.com", "recipient@redmath.com");
        assertThat(users.getAllValues())
                .extracting(AppUser::getName)
                .containsExactly("John Doe Sender", "Jane Smith Recipient");
        assertThat(users.getAllValues())
                .allSatisfy(user -> {
                    assertThat(user.getAddress()).isEqualTo("123");
                    assertThat(user.getRole()).isEqualTo(Role.ACCOUNT_HOLDER);
                    assertThat(user.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
                });

        ArgumentCaptor<BankAccount> accounts = ArgumentCaptor.forClass(BankAccount.class);
        verify(accountRepository, times(2)).save(accounts.capture());
        assertThat(accounts.getAllValues())
                .extracting(BankAccount::getAccountNumber)
                .containsExactly("PK1000000001", "PK2000000002");
        assertThat(accounts.getAllValues())
                .extracting(account -> account.getUser().getEmail())
                .containsExactly("sender@redmath.com", "recipient@redmath.com");
        assertThat(accounts.getAllValues())
                .allSatisfy(account -> assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE));

        ArgumentCaptor<AccountBalance> balances = ArgumentCaptor.forClass(AccountBalance.class);
        verify(balanceRepository, times(2)).save(balances.capture());
        List<AccountBalance> seededBalances = balances.getAllValues();
        assertThat(seededBalances)
                .extracting(AccountBalance::getAmount)
                .containsExactly(new BigDecimal("5000.00"), new BigDecimal("1000.00"));
        assertThat(seededBalances)
                .allSatisfy(balance -> assertThat(balance.getIndicator()).isEqualTo(BalanceIndicator.CREDIT));
    }

    private CommandLineRunner runner() {
        return initializer.initDatabase(userRepository, accountRepository, balanceRepository);
    }
}
