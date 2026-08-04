package com.redmath.bankapp.account.repository;


import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, String> {


    Optional<BankAccount> findByUser_Id(
            Long userId
    );

    boolean existsByUser_Id(
            Long userId
    );
    long countByStatus(AccountStatus status);
}