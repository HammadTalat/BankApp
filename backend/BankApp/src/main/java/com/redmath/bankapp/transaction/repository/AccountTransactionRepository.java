package com.redmath.bankapp.transaction.repository;

import com.redmath.bankapp.transaction.entity.AccountTransaction;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    /**
     * Fetches transaction history for a primary account number.
     * Aligned with your getUserTransactions(...) service call.
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    Page<AccountTransaction> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    /**
     * Complete Ledger View: Fetches transactions where the account was either the sender OR the recipient.
     * Useful if you want the user to see incoming transfers initiated by others.
     */
    @Query("SELECT t FROM AccountTransaction t " +
            "WHERE t.account.accountNumber = :accountNumber OR t.recipientAccount.accountNumber = :accountNumber " +
            "ORDER BY t.transactionDate DESC")
    Page<AccountTransaction> findAllTransactionsByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    /**
     * Fetch transactions associated with a specific operation ID (useful for idempotency checks/grouping).
     */
    List<AccountTransaction> findByOperationId(String operationId);

    /**
     * Check if a transaction with the given operation ID already exists.
     */
    boolean existsByOperationId(String operationId);

    /**
     * Fetch transactions within a date range for bank statements.
     */
    @Query("SELECT t FROM AccountTransaction t " +
            "WHERE t.account.accountNumber = :accountNumber " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    Page<AccountTransaction> findByAccountNumberAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    List<AccountTransaction> findTop20ByAccount_AccountNumberOrderByTransactionDateDesc(String accountNumber);
    /**
     * Calculates a debit-only spending summary in the database for one account and date range.
     */
    @Query("SELECT new com.redmath.bankapp.transaction.repository.SpendingAggregate(" +
            "SUM(t.amount), COUNT(t), MAX(t.amount)) " +
            "FROM AccountTransaction t " +
            "WHERE t.account.accountNumber = :accountNumber " +
            "AND t.indicator = :indicator " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    SpendingAggregate summarizeTransactionsByIndicatorAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("indicator") TransactionIndicator indicator,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
