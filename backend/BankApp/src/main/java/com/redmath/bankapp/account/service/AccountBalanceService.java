package com.redmath.bankapp.account.service;

import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class AccountBalanceService {


    private final AccountBalanceRepository accountBalanceRepository;
    private final BankAccountRepository bankAccountRepository;

    AccountBalanceService(AccountBalanceRepository accountBalanceRepository, BankAccountRepository bankAccountRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Jwt user) throws AccountNotFoundException {
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

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException("JWT does not contain a valid userId claim");
    }
}
