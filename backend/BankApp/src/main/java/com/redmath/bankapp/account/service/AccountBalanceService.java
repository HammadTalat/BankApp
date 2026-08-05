package com.redmath.bankapp.account.service;

import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

@Service
public class AccountBalanceService {


    private final AccountBalanceRepository accountBalanceRepository;
    private final BankAccountRepository bankAccountRepository;

    AccountBalanceService(AccountBalanceRepository accountBalanceRepository, BankAccountRepository bankAccountRepository ){
        this.accountBalanceRepository = accountBalanceRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UserPrincipal user) throws AccountNotFoundException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User principal and ID must not be null");
        }

        BankAccount bankAccount = bankAccountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new AccountNotFoundException("No bank account found for user ID: " + user.getId()));

        AccountBalance accountBalance = accountBalanceRepository.findLatestBalance(bankAccount.getAccountNumber())
                .orElseThrow(() -> new BalanceNotFoundException("Balance record not found for account: " + bankAccount.getAccountNumber()));

        BigDecimal amount = accountBalance.getAmount() != null ? accountBalance.getAmount() : BigDecimal.ZERO;

        return new BalanceResponse(amount);
    }
}
