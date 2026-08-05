package com.redmath.bankapp.account.controller;


import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.service.AccountBalanceService;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.AccountNotFoundException;

@RestController
@RequestMapping("/api/v1/account/")
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;

    public AccountBalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserPrincipal user) throws AccountNotFoundException {
        return ResponseEntity.ok(accountBalanceService.getBalance(user));
    }
}
