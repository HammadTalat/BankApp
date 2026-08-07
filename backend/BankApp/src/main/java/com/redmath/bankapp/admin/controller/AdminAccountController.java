package com.redmath.bankapp.admin.controller;


import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.admin.dto.response.AccountClosureResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountDetailsResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountSummaryResponse;
import com.redmath.bankapp.admin.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  public ResponseEntity<Page<AdminAccountSummaryResponse>> getAccounts(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) AccountStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    page = Math.max(page, 0);
    page = Math.min(page, 10);
    size = Math.max(size, 0);
    size = Math.min(size, 20);
    Pageable pageable = PageRequest.of(page, size);

    Page<AdminAccountSummaryResponse> accounts =
        adminAccountService.getAccounts(
            search,
            status,
            pageable
        );

    return ResponseEntity.ok(accounts);
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

  @PostMapping("/{accountNumber}/close")
  public ResponseEntity<AccountClosureResponse> closeAccount(
      @PathVariable String accountNumber
  ) {
    return ResponseEntity.ok(
        adminAccountService.closeAccount(accountNumber)
    );
  }
}