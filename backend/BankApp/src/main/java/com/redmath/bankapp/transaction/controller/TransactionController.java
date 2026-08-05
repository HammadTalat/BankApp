package com.redmath.bankapp.transaction.controller;

import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.dto.AccountLookupResponse;
import com.redmath.bankapp.transaction.dto.TransferRequest;
import com.redmath.bankapp.transaction.dto.TransferResponse;
import com.redmath.bankapp.transaction.dto.UserTransactionsResponse;
import com.redmath.bankapp.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction/")
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
    public ResponseEntity<TransferResponse> executeTransfer(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody TransferRequest request) throws AccountNotFoundException {
        TransferResponse response = transactionService.executeTransfer(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-transactions")
    public ResponseEntity<UserTransactionsResponse> getUserTransactions(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 10) Pageable pageable)
            throws AccountNotFoundException {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 instead of 500
        }

        UserTransactionsResponse response = transactionService.getUserTransactions(user,startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

}
