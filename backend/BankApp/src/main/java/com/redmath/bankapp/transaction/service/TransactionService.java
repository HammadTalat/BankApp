package com.redmath.bankapp.transaction.service;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.transaction.exception.BusinessRuleException;
import com.redmath.bankapp.transaction.exception.InsufficientBalanceException;
import com.redmath.bankapp.transaction.exception.UnauthorizedAccessException;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.dto.*;
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

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TransactionService {

    private final BankAccountRepository accountRepository;

    private final AccountBalanceRepository balanceRepository;

    private final AccountTransactionRepository transactionRepository;

    public TransactionService(BankAccountRepository accountRepository, AccountTransactionRepository  transactionRepository, AccountBalanceRepository balanceRepository) {
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
    public TransferResponse executeTransfer(UserPrincipal userPrincipal, TransferRequest request) throws AccountNotFoundException {
        log.info("Processing transfer of {} from {} to {}",
                request.amount(), request.senderAccountNumber(), request.receiverAccountNumber());

        // 1. Prevent self-transfer
        if (request.senderAccountNumber().equalsIgnoreCase(request.receiverAccountNumber())) {
            throw new BusinessRuleException("Sender and receiver accounts cannot be the same");
        }

        // 2. Fetch sender account & verify security principal ownership
        BankAccount senderAccount = accountRepository.findById(request.senderAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        validateAccountOwnership(senderAccount, userPrincipal);
        validateAccountActive(senderAccount, "Sender");

        // 3. Fetch receiver account
        BankAccount receiverAccount = accountRepository.findById(request.receiverAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        validateAccountActive(receiverAccount, "Receiver");

        // 4. Pessimistically Lock Sender Balance to prevent Race Conditions (Double-Spend)
        AccountBalance senderBalance = balanceRepository.findLatestBalanceForUpdate(senderAccount.getAccountNumber())
                .orElseThrow(() -> new BusinessRuleException("Sender balance record missing"));

        // 5. Check funds sufficiency
        if (senderBalance.getAmount().compareTo(request.amount()) < 0) {
            log.warn("Transfer failed: Insufficient funds in account {}", senderAccount.getAccountNumber());
            throw new InsufficientBalanceException("Insufficient balance to perform this transfer");
        }

        // 6. Lock and fetch Receiver Balance
        AccountBalance receiverBalance = balanceRepository.findLatestBalanceForUpdate(receiverAccount.getAccountNumber())
                .orElseThrow(() -> new BusinessRuleException("Receiver balance record missing"));

        // 7. Update Balances
        BigDecimal newSenderAmount = senderBalance.getAmount().subtract(request.amount());
        BigDecimal newReceiverAmount = receiverBalance.getAmount().add(request.amount());

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
        String operationId = "op-" + UUID.randomUUID();

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


    @Transactional(readOnly = true)
    public UserTransactionsResponse getUserTransactions(UserPrincipal userPrincipal, Pageable pageable) throws AccountNotFoundException {
        log.debug("Fetching transaction history for user ID: {}", userPrincipal.getId());

        BankAccount account = accountRepository.findByUser_Id(userPrincipal.getId())
                .orElseThrow(() -> new AccountNotFoundException("No account linked to the current user"));

        Page<AccountTransaction> transactionsPage = transactionRepository
                .findByAccountNumber(account.getAccountNumber(), pageable);

        Page<TransactionResponse> dtoPage = transactionsPage.map(TransactionResponse::fromEntity);

        return UserTransactionsResponse.fromPage(dtoPage);
    }


    private void validateAccountOwnership(BankAccount account, UserPrincipal principal) {
        if (!account.getUser().getId().equals(principal.getId())) {
            log.error("Security violation: User {} attempted operation on unowned account {}",
                    principal.getId(), account.getAccountNumber());
            throw new UnauthorizedAccessException("You are not authorized to execute transactions for this account");
        }
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



}
