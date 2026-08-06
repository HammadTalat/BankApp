package com.redmath.bankapp.account.controller;


import com.redmath.bankapp.account.dto.AccountResponse;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.AccountNotFoundException;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal Jwt jwt) throws AccountNotFoundException {
        return ResponseEntity.ok(accountService.getBalance(jwt));
    }

    @GetMapping()
    public  ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal Jwt jwt) throws AccountNotFoundException
    {
        return ResponseEntity.ok(accountService.getAccount(jwt));
    }

}
