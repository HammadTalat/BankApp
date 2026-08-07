package com.redmath.bankapp.admin.service;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.AccountClosureResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountDetailsResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountSummaryResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountTransactionsResponse;
import com.redmath.bankapp.admin.exception.InvalidAccountStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.transaction.dto.TransactionResponse;
import com.redmath.bankapp.transaction.repository.AccountTransactionRepository;
import com.redmath.bankapp.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    @Transactional(readOnly = true)
    public Page<AdminAccountSummaryResponse> getAccounts(
            String search,
            AccountStatus status,
            Pageable pageable
    ) {
        String searchValue = prepareSearchValue(search);

        Page<BankAccount> accounts;

        if (searchValue == null && status == null) {
            accounts = bankAccountRepository.findAll(pageable);

        } else if (searchValue == null) {
            accounts = bankAccountRepository.findAllByStatus(
                    status,
                    pageable
            );

        } else{
            accounts = bankAccountRepository.searchAccounts(
                    searchValue,
                    status,
                    pageable
            );

        }

        return accounts.map(this::toSummaryResponse);
    }
    private String prepareSearchValue(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    @Transactional(readOnly = true)
    public AdminAccountDetailsResponse getAccountDetails(
            String accountNumber
    ) {
        BankAccount account = findAccount(accountNumber);
        AccountBalance balance = findLatestBalance(accountNumber);
        AppUser user = account.getUser();

        return new AdminAccountDetailsResponse(
                account.getAccountNumber(),
                account.getStatus(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAddress(),
                user.getApprovalStatus(),
                balance.getAmount(),
                balance.getIndicator(),
                balance.getBalanceDate()
        );
    }

    private AdminAccountSummaryResponse toSummaryResponse(
            BankAccount account
    ) {
        AccountBalance balance = findLatestBalance(
                account.getAccountNumber()
        );

        AppUser user = account.getUser();

        return new AdminAccountSummaryResponse(
                account.getAccountNumber(),
                account.getStatus(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                balance.getAmount()
        );
    }

    private BankAccount findAccount(String accountNumber) {
        return bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found: " + accountNumber
                ));
    }

    private AccountBalance findLatestBalance(
            String accountNumber
    ) {
        return accountBalanceRepository
                .findLatestBalance(
                        accountNumber
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Balance not found for account: " + accountNumber
                ));
    }
    @Transactional
    public AccountClosureResponse closeAccount(
            String accountNumber
    ) {
        BankAccount account = findAccount(accountNumber);

        validateAccountIsActive(account);

        AccountBalance latestBalance =
                findLatestBalance(accountNumber);

        validateZeroBalance(latestBalance);

        account.close();

        BankAccount closedAccount =
                bankAccountRepository.save(account);

        return new AccountClosureResponse(
                closedAccount.getAccountNumber(),
                closedAccount.getStatus(),
                latestBalance.getAmount()
        );
    }
    private void validateZeroBalance(
            AccountBalance balance
    ) {
        boolean hasRemainingBalance =
                balance.getAmount().compareTo(BigDecimal.ZERO) != 0;

        if (hasRemainingBalance) {
            throw new InvalidAccountStateException(
                    "Account balance must be zero before closing"
            );
        }
    }
    private void validateAccountIsActive(
            BankAccount account
    ) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException(
                    "Only active accounts can be closed"
            );
        }
    }
    @Transactional(readOnly = true)
    public AdminAccountTransactionsResponse getAccountTransactions(
            String accountNumber,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        findAccount(accountNumber);

        if (
                startDate != null
                        && endDate != null
                        && startDate.isAfter(endDate)
        ) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        LocalDateTime start =
                startDate == null
                        ? null
                        : startDate.atStartOfDay();

        LocalDateTime end =
                endDate == null
                        ? null
                        : endDate.atTime(LocalTime.MAX);

        Page<TransactionResponse> transactions =
                accountTransactionRepository
                        .findByAccountNumberAndDateRange(

                                accountNumber,
                                start,
                                end,
                                pageable
                        )
                        .map(TransactionResponse::fromEntity);

        return AdminAccountTransactionsResponse
                .fromPage(transactions);
    }
}
