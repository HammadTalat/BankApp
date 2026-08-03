package com.redmath.bankapp.transaction.controller;

import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction/")
public class TransactionController {


    @GetMapping("/lookup")
    public ResponseEntity<AccountLookupResponse> lookupAccount(
            @RequestParam @NotBlank(message = "Account number is required") String accountID) {
        AccountLookupResponse response = transactionService.lookupAccount(accountID);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(@AuthenticationPrincipal UserPrincipal user, @Valid TransferRequest request) {
        TransferResponse response = transactionService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<UserTransactionsResponse> getUserTransactions(@AuthenticationPrincipal UserPrincipal user, @PageableDefault(page = 0, size = 10)) {
        UserTransactionsResponse response = transactionService.getUserTransactions(user);
        return ResponseEntity.ok(response);
    }

}
