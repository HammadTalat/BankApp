package com.redmath.bankapp.transaction.service;


import com.redmath.bankapp.transaction.dto.AccountLookupResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;

@Slf4j
@Service
public class TransactionService {


    @Transactional(readOnly = true)
    public AccountLookupResponse lookupAccount(String accountID) {
        log.debug("Initiating account lookup for account identifier: {}", accountID);

        BankAccount account = accountRepository.findByAccountNumber(accountID)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with identifier: " + accountID));

        return AccountLookupResponse.fromEntity(account);
    }

}
