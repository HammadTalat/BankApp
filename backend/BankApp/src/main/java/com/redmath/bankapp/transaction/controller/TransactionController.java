package com.redmath.bankapp.transaction.controller;

import com.redmath.bankapp.transaction.dto.*;
import com.redmath.bankapp.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/lookup")
    public ResponseEntity<AccountLookupResponse> lookupAccount(
            @RequestParam @NotBlank(message = "Account number is required") String accountID) throws AccountNotFoundException {
        AccountLookupResponse response = transactionService.lookupAccount(accountID);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> executeTransfer(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TransferRequest request)
        throws AccountNotFoundException {
        TransferResponse response = transactionService.executeTransfer(jwt, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> executeDeposit(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DepositRequest request) throws AccountNotFoundException {
        DepositResponse response = transactionService.executeDeposit(jwt, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/get-transactions")
    public ResponseEntity<UserTransactionsResponse> getUserTransactions(@AuthenticationPrincipal Jwt jwt,
                                                                        @RequestParam(value = "startDate", required = false)
                                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                        @RequestParam(value = "endDate", required = false)
                                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                        @PageableDefault(page = 0, size = 10) Pageable pageable) throws AccountNotFoundException {
      UserTransactionsResponse response = transactionService.getUserTransactions(jwt, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

}
