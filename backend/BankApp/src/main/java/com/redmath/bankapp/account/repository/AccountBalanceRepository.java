package com.redmath.bankapp.account.repository;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountBalanceRepository
        extends JpaRepository<AccountBalance, Long> {

    Optional<AccountBalance> findByAccount_AccountNumber(
            String accountNumber
    );

    boolean existsByAccount_AccountNumber(
            String accountNumber
    );
}