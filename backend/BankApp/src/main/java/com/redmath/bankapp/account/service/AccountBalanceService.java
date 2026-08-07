package com.redmath.bankapp.account.service;

import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import java.math.BigDecimal;
import javax.security.auth.login.AccountNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountBalanceService {


  private final AccountBalanceRepository accountBalanceRepository;
  private final BankAccountRepository bankAccountRepository;

  AccountBalanceService(AccountBalanceRepository accountBalanceRepository,
      BankAccountRepository bankAccountRepository) {
    this.accountBalanceRepository = accountBalanceRepository;
    this.bankAccountRepository = bankAccountRepository;
  }

  @Transactional(readOnly = true)
  public BalanceResponse getBalance(Jwt jwt) throws AccountNotFoundException {
    if (jwt == null || jwt.getSubject() == null) {
      throw new IllegalArgumentException("Jwt and subject must not be null");
    }

    Long userId = extractUserId(jwt);

    BankAccount bankAccount = bankAccountRepository.findByUser_Id(userId)
        .orElseThrow(
            () -> new AccountNotFoundException("No bank account found for user ID: " + userId));

    AccountBalance accountBalance = accountBalanceRepository.findLatestBalance(
            bankAccount.getAccountNumber())
        .orElseThrow(() -> new BalanceNotFoundException(
            "Balance record not found for account: " + bankAccount.getAccountNumber()));

    BigDecimal amount =
        accountBalance.getAmount() != null ? accountBalance.getAmount() : BigDecimal.ZERO;

    return new BalanceResponse(amount);
  }

  private Long extractUserId(Jwt jwt) {
    Object userIdClaim = jwt.getClaims().get("userId");

    if (userIdClaim instanceof Number number) {
      return number.longValue();
    }

    throw new IllegalStateException("JWT does not contain a valid userId claim");
  }
}
