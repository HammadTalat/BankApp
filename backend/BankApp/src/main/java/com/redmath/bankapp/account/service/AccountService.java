package com.redmath.bankapp.account.service;

import com.redmath.bankapp.account.dto.AccountResponse;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

@Service
public class AccountService {


    private final AccountBalanceRepository accountBalanceRepository;
    private final BankAccountRepository bankAccountRepository;

    AccountService(AccountBalanceRepository accountBalanceRepository, BankAccountRepository bankAccountRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Jwt user) throws AccountNotFoundException, BalanceNotFoundException {
        if (user == null) {
            throw new IllegalArgumentException("User principal and ID must not be null");
        }

        Long userId = extractUserId(user);

        BankAccount bankAccount = bankAccountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AccountNotFoundException("No bank account found for user ID: " + userId));

        BigDecimal amount = accountBalanceRepository.findLatestBalance(bankAccount.getAccountNumber())
                .map(AccountBalance::getAmount)
                .orElse(BigDecimal.ZERO);

        return new BalanceResponse(amount);
    }

    public AccountResponse getAccount(Jwt jwt) throws AccountNotFoundException {
        BankAccount account = bankAccountRepository.findByUser_Id(extractUserId(jwt)).orElseThrow(() -> new AccountNotFoundException("No bank account found for user"));
        return new AccountResponse(account.getAccountNumber(), account.getStatus());
    }

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException("JWT does not contain a valid userId claim");
    }


}
