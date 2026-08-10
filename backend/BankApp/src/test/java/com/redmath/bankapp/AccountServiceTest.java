package com.redmath.bankapp;

import com.redmath.bankapp.account.dto.AccountResponse;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private AccountService accountBalanceService;

    @InjectMocks
    private AccountService accountService;

    private Jwt validJwtPrincipal;
    private BankAccount bankAccount;
    private AccountBalance accountBalance;

    private final AccountStatus accountStatus = AccountStatus.ACTIVE;

    private final Long userId = 1L;
    private final String accountNumber = "ACC123456";

    @BeforeEach
    void setUp() {

        validJwtPrincipal = Jwt.withTokenValue("mock-jwt-token-string")
                .header("alg", "HS256")
                .header("typ", "JWT")
                .subject(String.valueOf(userId))
                .issuer("http://localhost:8080")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("userId", userId)
                .claim("email", "test@redmath.com")
                .build();


        bankAccount = new BankAccount();
        bankAccount.setAccountNumber(accountNumber);

        accountBalance = new AccountBalance(bankAccount, new BigDecimal("1500.50"), BalanceIndicator.CREDIT);
    }

    @Test
    @DisplayName("Should return BalanceResponse when user has valid account and balance")
    void getBalance_ValidUser_ReturnsBalanceResponse() throws AccountNotFoundException {
        given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.of(bankAccount));
        given(accountBalanceRepository.findLatestBalance(accountNumber)).willReturn(Optional.of(accountBalance));

        BalanceResponse response = accountBalanceService.getBalance(validJwtPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("1500.50"));

        verify(bankAccountRepository).findByUser_Id(userId);
        verify(accountBalanceRepository).findLatestBalance(accountNumber);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when UserPrincipal is null")
    void getBalance_NullUserPrincipal_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> accountBalanceService.getBalance(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User principal and ID must not be null");

        verifyNoInteractions(bankAccountRepository, accountBalanceRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when JWT userId claim is null")
    void getBalance_NullUserId_ThrowsIllegalArgumentException() {
        // Arrange
        Jwt nullJwtPrincipal = Jwt.withTokenValue("mock-jwt-token-string")
                .header("alg", "HS256")
                .header("typ", "JWT")
                .subject(null) // Or omit if subject isn't required by your JWT builder
                .issuer("http://localhost:8080")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("userId", null) // Explicitly null userId claim
                .claim("email", "test@redmath.com")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> accountBalanceService.getBalance(nullJwtPrincipal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT does not contain a valid userId claim");

        verifyNoInteractions(bankAccountRepository, accountBalanceRepository);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when user has no bank account")
    void getBalance_AccountNotFound_ThrowsAccountNotFoundException() {
        given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountBalanceService.getBalance(validJwtPrincipal))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("No bank account found for user ID: " + userId);

        verify(bankAccountRepository).findByUser_Id(userId);
        verifyNoInteractions(accountBalanceRepository);
    }

    @Test
    @DisplayName("Should return zero balance when balance record does not exist")
    void getBalance_BalanceNotFound_ReturnsZeroBalance() throws Exception {
        given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.of(bankAccount));
        given(accountBalanceRepository.findLatestBalance(accountNumber)).willReturn(Optional.empty());

        BalanceResponse response = accountBalanceService.getBalance(validJwtPrincipal);

        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(bankAccountRepository).findByUser_Id(userId);
        verify(accountBalanceRepository).findLatestBalance(accountNumber);
    }

    @Nested
    @DisplayName("getAccount Unit Tests")
    class GetAccountTests {

        @Test
        @DisplayName("Should return AccountResponse when bank account exists for user")
        void getAccount_AccountExists_ReturnsAccountResponse() throws Exception {
            // Arrange
            BankAccount mockAccount = new BankAccount();
            mockAccount.setAccountNumber(accountNumber);
            mockAccount.setStatus(accountStatus);

            given(bankAccountRepository.findByUser_Id(any(Long.class))).willReturn(Optional.of(mockAccount));

            // Act
            AccountResponse response = accountService.getAccount(validJwtPrincipal);

            // Assert - Explicit property assertions kill return/mapping object mutations
            assertThat(response).isNotNull();
            assertThat(response.accountNumber()).isEqualTo(accountNumber); // or response.getAccountNumber()
            assertThat(response.status()).isEqualTo(accountStatus);       // or response.getStatus()

            // Verify interactions to kill boundary/side-effect mutations
            verify(bankAccountRepository).findByUser_Id(userId);
            verifyNoMoreInteractions(bankAccountRepository);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException with exact message when account does not exist")
        void getAccount_AccountNotFound_ThrowsAccountNotFoundException() {
            // Arrange

            given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.empty());
            // Act & Assert
            // Asserting exact class and message kills lambda/exception message mutations
            assertThatThrownBy(() -> accountService.getAccount(validJwtPrincipal))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessage("No bank account found for user");

            verify(bankAccountRepository).findByUser_Id(userId);
            verifyNoMoreInteractions(bankAccountRepository);
        }
    }
}