package com.redmath.bankapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.account.service.AccountBalanceService;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import javax.security.auth.login.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

  private final Long userId = 1L;
  private final String accountNumber = "ACC123456";
  @Mock
  private BankAccountRepository bankAccountRepository;
  @Mock
  private AccountBalanceRepository accountBalanceRepository;
  @InjectMocks
  private AccountBalanceService accountBalanceService;
  private Jwt mockJwt;
  private BankAccount bankAccount;
  private AccountBalance accountBalance;

  @BeforeEach
  void setUp() {
    mockJwt = new Jwt(
        "token",
        null,
        null,
        Map.of("alg", "PS256"),
        Map.of("sub", "test@redmath.com", "userId", userId)
    );

    bankAccount = new BankAccount();
    bankAccount.setAccountNumber(accountNumber);

    accountBalance = new AccountBalance(bankAccount, new BigDecimal("1500.50"),
        BalanceIndicator.CREDIT);
  }

  @Test
  @DisplayName("Should return BalanceResponse when user has valid account and balance")
  void getBalance_ValidUser_ReturnsBalanceResponse() throws AccountNotFoundException {
    given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.of(bankAccount));
    given(accountBalanceRepository.findLatestBalance(accountNumber)).willReturn(
        Optional.of(accountBalance));

    BalanceResponse response = accountBalanceService.getBalance(mockJwt);

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("1500.50"));

    verify(bankAccountRepository).findByUser_Id(userId);
    verify(accountBalanceRepository).findLatestBalance(accountNumber);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when Jwt is null")
  void getBalance_NullJwt_ThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> accountBalanceService.getBalance(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Jwt and subject must not be null");

    verifyNoInteractions(bankAccountRepository, accountBalanceRepository);
  }

  @Test
  @DisplayName("Should throw IllegalStateException when Jwt has no userId claim")
  void getBalance_MissingUserId_ThrowsIllegalStateException() {
    Jwt jwtWithoutUserId = new Jwt(
        "token",
        null,
        null,
        Map.of("alg", "PS256"),
        Map.of("sub", "test@redmath.com")
    );

    assertThatThrownBy(() -> accountBalanceService.getBalance(jwtWithoutUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("JWT does not contain a valid userId claim");

    verifyNoInteractions(bankAccountRepository, accountBalanceRepository);
  }

  @Test
  @DisplayName("Should throw AccountNotFoundException when user has no bank account")
  void getBalance_AccountNotFound_ThrowsAccountNotFoundException() {
    given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> accountBalanceService.getBalance(mockJwt))
        .isInstanceOf(AccountNotFoundException.class)
        .hasMessage("No bank account found for user ID: " + userId);

    verify(bankAccountRepository).findByUser_Id(userId);
    verifyNoInteractions(accountBalanceRepository);
  }

  @Test
  @DisplayName("Should throw BalanceNotFoundException when balance record does not exist")
  void getBalance_BalanceNotFound_ThrowsBalanceNotFoundException() {
    given(bankAccountRepository.findByUser_Id(userId)).willReturn(Optional.of(bankAccount));
    given(accountBalanceRepository.findLatestBalance(accountNumber)).willReturn(Optional.empty());

    assertThatThrownBy(() -> accountBalanceService.getBalance(mockJwt))
        .isInstanceOf(BalanceNotFoundException.class)
        .hasMessage("Balance record not found for account: " + accountNumber);

    verify(bankAccountRepository).findByUser_Id(userId);
    verify(accountBalanceRepository).findLatestBalance(accountNumber);
  }
}
