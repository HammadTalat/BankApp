package com.redmath.bankapp.account.repository;


import com.redmath.bankapp.account.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}