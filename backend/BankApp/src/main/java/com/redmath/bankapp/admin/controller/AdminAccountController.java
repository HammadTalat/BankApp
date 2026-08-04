package com.redmath.bankapp.admin.controller;


import com.redmath.bankapp.admin.dto.response.AdminAccountDetailsResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountSummaryResponse;
import com.redmath.bankapp.admin.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAXIMUM_PAGE_SIZE = 20;

    private final AdminAccountService adminAccountService;

    @GetMapping
    public ResponseEntity<Page<AdminAccountSummaryResponse>>
    getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(
                    defaultValue = "" + DEFAULT_PAGE_SIZE
            ) int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 1),
                MAXIMUM_PAGE_SIZE
        );

        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("accountNumber").ascending()
        );

        return ResponseEntity.ok(
                adminAccountService.getAllAccounts(pageRequest)
        );
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AdminAccountDetailsResponse>
    getAccountDetails(
            @PathVariable String accountNumber
    ) {
        return ResponseEntity.ok(
                adminAccountService.getAccountDetails(accountNumber)
        );
    }
}