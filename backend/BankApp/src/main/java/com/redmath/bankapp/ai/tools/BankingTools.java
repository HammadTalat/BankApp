package com.redmath.bankapp.ai.tools;

import com.redmath.bankapp.account.dto.AccountResponse;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.service.AccountService;
import com.redmath.bankapp.transaction.dto.UserTransactionsResponse;
import com.redmath.bankapp.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BankingTools {

  private final AccountService accountService;
  private final TransactionService transactionService;


  @Tool(description = "Get the authenticated user's bank account summary including account number,"
      + " status, and current balance.")
  public String getMyAccountSummary() {
    try {
      Jwt jwt = currentJwt();
      AccountResponse account = accountService.getAccount(jwt);
      BalanceResponse balance = accountService.getBalance(jwt);
      return String.format(
          "Account Number: %s%nStatus: %s%nCurrent Balance: $%.2f",
          account.accountNumber(),
          account.status(),
          balance.amount()
      );
    } catch (AccountNotFoundException e) {
      return "I could not retrieve your account information at this time. "
          + "Please contact support if the issue persists.";
    } catch (Exception e) {
      return "An unexpected error occurred while retrieving your account summary. "
          + "Please try again later.";
    }
  }

  @Tool(description = "Get the authenticated user's recent transactions. "
      + "Provide a limit for how many transactions to show (max 20).")
  public String getRecentTransactions(int limit) {
    try {
      Jwt jwt = currentJwt();
      int effectiveLimit = Math.min(Math.max(limit, 1), 20);
      LocalDate endDate = LocalDate.now();
      LocalDate startDate = endDate.minusDays(30);

      UserTransactionsResponse response = transactionService.getUserTransactions(
          jwt,
          startDate,
          endDate,
          PageRequest.of(0, effectiveLimit, Sort.by(Sort.Direction.DESC, "transactionDate"))
      );

      if (response.transactions() == null || response.transactions().isEmpty()) {
        return "No transactions found in the last 30 days.";
      }

      StringBuilder sb = new StringBuilder("Your recent transactions:\n");
      response.transactions().forEach(tx -> sb.append(String.format(
          "- %s | %s | $%.2f | %s%n",
          tx.transactionDate() != null ? tx.transactionDate().toLocalDate() : "N/A",
          tx.description() != null ? tx.description() : "No description",
          tx.amount(),
          tx.indicator()
      )));
      return sb.toString();
    } catch (AccountNotFoundException e) {
      return "I could not retrieve your transactions at this time. "
          + "Please contact support if the issue persists.";
    } catch (Exception e) {
      return "An unexpected error occurred while retrieving your transactions. "
          + "Please try again later.";
    }
  }

  private Jwt currentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt principalJwt)) {
      throw new IllegalStateException("No authenticated user context is available.");
    }
    return principalJwt;
  }
}
