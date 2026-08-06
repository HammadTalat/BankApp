package com.redmath.bankapp.account.repository;


import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, String> {


    Optional<BankAccount> findByUser_Id(
            Long userId
    );

    boolean existsByUser_Id(
            Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.accountNumber = :accountNumber")
    Optional<BankAccount> findByIdForUpdate(@Param("accountNumber") String accountNumber);
    long countByStatus(AccountStatus status);
    @Query("""
        SELECT account
        FROM BankAccount account
        JOIN account.user user
        WHERE
            (:status IS NULL OR account.status = :status)
            AND
            (
                :search IS NULL
                OR LOWER(account.accountNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(user.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(user.email) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        """)
    Page<BankAccount> searchAccounts(
            String search,
            AccountStatus status,
            Pageable pageable
    );
    Page<BankAccount> findAllByStatus(
            AccountStatus status,
            Pageable pageable
    );
}