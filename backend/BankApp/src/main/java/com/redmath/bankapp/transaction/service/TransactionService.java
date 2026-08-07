package com.redmath.bankapp.transaction.service;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.transaction.dto.*;
import com.redmath.bankapp.transaction.exception.BusinessRuleException;
import com.redmath.bankapp.transaction.exception.InsufficientBalanceException;
import com.redmath.bankapp.transaction.exception.UnauthorizedAccessException;
import com.redmath.bankapp.transaction.entity.AccountTransaction;
import com.redmath.bankapp.transaction.enums.OperationStatus;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;
import com.redmath.bankapp.transaction.repository.AccountTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
public class TransactionService {

    private final BankAccountRepository accountRepository;

    private final AccountBalanceRepository balanceRepository;

    private final AccountTransactionRepository transactionRepository;

    public TransactionService(BankAccountRepository accountRepository, AccountTransactionRepository transactionRepository, AccountBalanceRepository balanceRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.balanceRepository = balanceRepository;
    }


    @Transactional(readOnly = true)
    public AccountLookupResponse lookupAccount(String accountID) throws AccountNotFoundException {
        log.debug("Initiating account lookup for account identifier: {}", accountID);

        BankAccount account = accountRepository.findById(accountID)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with identifier: " + accountID));

        return AccountLookupResponse.fromEntity(account);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransferResponse executeTransfer(Jwt jwt, TransferRequest request) throws AccountNotFoundException {
        log.info("Processing transfer of {} from {} to {}",
                request.amount(), request.senderAccountNumber(), request.receiverAccountNumber());

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Transfer amount must be greater than zero");
        }

        // 1. Prevent self-transfer
        if (request.senderAccountNumber().equalsIgnoreCase(request.receiverAccountNumber())) {
            throw new BusinessRuleException("Sender and receiver accounts cannot be the same");
        }

        // 2. Lock accounts in deterministic order to prevent DB deadlocks
        boolean senderFirst = request.senderAccountNumber().compareTo(request.receiverAccountNumber()) < 0;
        String firstAcc = senderFirst ? request.senderAccountNumber() : request.receiverAccountNumber();
        String secondAcc = senderFirst ? request.receiverAccountNumber() : request.senderAccountNumber();

        // Lock both parent accounts first (FOR UPDATE on bank_accounts table)
        BankAccount firstAccount = accountRepository.findByIdForUpdate(firstAcc)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + firstAcc));
        BankAccount secondAccount = accountRepository.findByIdForUpdate(secondAcc)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + secondAcc));

        BankAccount senderAccount = senderFirst ? firstAccount : secondAccount;
        BankAccount receiverAccount = senderFirst ? secondAccount : firstAccount;

        validateAccountOwnership(senderAccount, extractUserId(jwt));
        validateAccountActive(senderAccount, "Sender");
        validateAccountActive(receiverAccount, "Receiver");

        // 4. Lock Sender Balance (Default to ZERO if no record exists)
        BigDecimal currentSenderAmount = balanceRepository
                .findLatestBalanceForUpdate(senderAccount.getAccountNumber())
                .map(AccountBalance::getAmount)
                .orElse(BigDecimal.ZERO);

        // 5. Check funds sufficiency
        if (currentSenderAmount.compareTo(request.amount()) < 0) {
            log.warn("Transfer failed: Insufficient funds in account {}", senderAccount.getAccountNumber());
            throw new InsufficientBalanceException("Insufficient balance to perform this transfer");
        }

        // 6. Lock and fetch Receiver Balance
        BigDecimal currentReceiverAmount = balanceRepository
                .findLatestBalanceForUpdate(receiverAccount.getAccountNumber())
                .map(AccountBalance::getAmount)
                .orElse(BigDecimal.ZERO);

        // 7. Update Balances
        BigDecimal newSenderAmount = currentSenderAmount.subtract(request.amount());
        BigDecimal newReceiverAmount = currentReceiverAmount.add(request.amount());

        AccountBalance newSenderLedgerEntry = new AccountBalance(
                senderAccount,
                newSenderAmount,
                BalanceIndicator.DEBIT
        );

        AccountBalance newReceiverLedgerEntry = new AccountBalance(
                receiverAccount,
                newReceiverAmount,
                BalanceIndicator.CREDIT
        );

        // Inserts new rows into account_balance table
        balanceRepository.save(newSenderLedgerEntry);
        balanceRepository.save(newReceiverLedgerEntry);

        // 8. Generate Audit Ledger Entries (Operation ID connects DEBIT & CREDIT records)
        String operationId = UUID.randomUUID().toString();

        AccountTransaction debitRecord = createLedgerRecord(
                senderAccount, receiverAccount, request.amount(),
                TransactionIndicator.DEBIT, request.description(), operationId);

        AccountTransaction creditRecord = createLedgerRecord(
                receiverAccount, senderAccount, request.amount(),
                TransactionIndicator.CREDIT, request.description(), operationId);

        transactionRepository.save(debitRecord);
        transactionRepository.save(creditRecord);

        log.info("Transfer successful. Operation ID: {}", operationId);

        return new TransferResponse(
                operationId,
                OperationStatus.COMPLETED,
                request.amount(),
                senderAccount.getAccountNumber(),
                receiverAccount.getAccountNumber(),
                request.description(),
                LocalDateTime.now()
        );
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public DepositResponse executeDeposit(Jwt jwt, DepositRequest request) throws AccountNotFoundException {
        log.info("Processing deposit of {} into account {}", request.amount(), request.accountNumber());

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Transfer amount must be greater than zero");
        }

        // 1. Lock target parent account (FOR UPDATE on bank_accounts table)
        BankAccount targetAccount = accountRepository.findByIdForUpdate(request.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.accountNumber()));

        // 2. Validate security and state
        validateAccountOwnership(targetAccount, extractUserId(jwt));
        validateAccountActive(targetAccount, "Target");

        // 3. Lock and fetch current balance, defaulting to ZERO if this is a newly created account
        AccountBalance currentBalance = balanceRepository.findLatestBalanceForUpdate(targetAccount.getAccountNumber())
                .orElseGet(() -> new AccountBalance(
                        targetAccount,
                        BigDecimal.ZERO,
                        BalanceIndicator.CREDIT
                ));

        // 4. Calculate new balance safely
        BigDecimal currentAmount = currentBalance.getAmount() != null ? currentBalance.getAmount() : BigDecimal.ZERO;
        BigDecimal newAmount = currentAmount.add(request.amount());

        // 5. Create immutable balance ledger entry
        AccountBalance newLedgerEntry = new AccountBalance(
                targetAccount,
                newAmount,
                BalanceIndicator.CREDIT
        );
        balanceRepository.save(newLedgerEntry);

        // 6. Generate single audit transaction record
        String operationId = UUID.randomUUID().toString();
        AccountTransaction depositTransaction = createDepositLedgerRecord(
                targetAccount,
                request.amount(),
                request.description() != null ? request.description() : "Cash Deposit",
                operationId
        );
        transactionRepository.save(depositTransaction);

        log.info("Deposit successful. Operation ID: {}", operationId);

        return new DepositResponse(
                operationId,
                OperationStatus.COMPLETED,
                request.amount(),
                targetAccount.getAccountNumber(),
                newAmount,
                request.description() != null ? request.description() : "Cash Deposit",
                LocalDateTime.now()
        );
    }


    @Transactional(readOnly = true)
    public UserTransactionsResponse getUserTransactions(Jwt jwt,
                                                        LocalDate startDate,
                                                        LocalDate endDate,
                                                        Pageable pageable) throws AccountNotFoundException {
        Long userId = extractUserId(jwt);
        log.debug("Fetching transaction history for user ID: {}", userId);

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        BankAccount account = accountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AccountNotFoundException("No account linked to the current user"));

        Page<AccountTransaction> transactionsPage = transactionRepository.findByAccountNumberAndDateRange(
                account.getAccountNumber(), start, end, pageable);

        Page<TransactionResponse> dtoPage = transactionsPage.map(TransactionResponse::fromEntity);

        return UserTransactionsResponse.fromPage(dtoPage);
    }


    private void validateAccountOwnership(BankAccount account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            log.error("Security violation: User {} attempted operation on unowned account {}",
                    userId, account.getAccountNumber());
            throw new UnauthorizedAccessException("You are not authorized to execute transactions for this account");
        }
    }

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException("JWT does not contain a valid userId claim");
    }

    private void validateAccountActive(BankAccount account, String context) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException(context + " account is not active (Status: " + account.getStatus() + ")");
        }
    }

    private AccountTransaction createLedgerRecord(
            BankAccount primaryAcc,
            BankAccount counterpartyAcc,
            BigDecimal amount,
            TransactionIndicator indicator,
            String description,
            String operationId) {

        AccountTransaction tx = new AccountTransaction();
        tx.setAccount(primaryAcc);
        tx.setRecipientAccount(counterpartyAcc);
        tx.setAmount(amount);
        tx.setIndicator(indicator);
        tx.setDescription(description);
        tx.setOperationId(operationId);
        tx.setTransactionDate(LocalDateTime.now());
        return tx;
    }

    private AccountTransaction createDepositLedgerRecord(
            BankAccount targetAccount,
            BigDecimal amount,
            String description,
            String operationId) {

        return AccountTransaction.builder()
                .account(targetAccount)
                .recipientAccount(null) // Null for deposits as there is no secondary counterparty account
                .amount(amount)
                .indicator(TransactionIndicator.CREDIT)
                .description(description != null && !description.isBlank() ? description : "Account Deposit")
                .operationId(operationId)
                .build();
    }


}
