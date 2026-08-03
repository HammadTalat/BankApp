package com.redmath.bankapp.account.repository;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountBalanceRepository
        extends JpaRepository<AccountBalance, Long> {

    Optional<AccountBalance> findByAccount_AccountNumber(
            String accountNumber
    );

    boolean existsByAccount_AccountNumber(
            String accountNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM AccountBalance b WHERE b.account.accountNumber = :accountNumber ORDER BY b.id DESC LIMIT 1")
    Optional<AccountBalance> findLatestBalanceForUpdate(@Param("accountNumber") String accountNumber);

}