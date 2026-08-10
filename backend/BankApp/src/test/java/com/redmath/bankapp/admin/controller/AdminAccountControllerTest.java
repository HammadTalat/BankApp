package com.redmath.bankapp.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.admin.dto.response.AccountClosureResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountDetailsResponse;
import com.redmath.bankapp.admin.dto.response.AdminAccountSummaryResponse;
import com.redmath.bankapp.admin.exception.AdminExceptionHandler;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.admin.service.AdminAccountService;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class   AdminAccountControllerTest {

    private static final Long USER_ID = 1L;
    private static final String ACCOUNT_NUMBER = "5839201746382915";
    private static final String EMAIL = "ali@example.com";

    private AdminAccountService accountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accountService = mock(AdminAccountService.class);
        AdminAccountController controller =
                new AdminAccountController(accountService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AdminExceptionHandler())
                .build();
    }

    @Test
    void shouldListAccountsUsingRequestParameters() throws Exception {
        AdminAccountSummaryResponse account =
                new AdminAccountSummaryResponse(
                        ACCOUNT_NUMBER,
                        AccountStatus.ACTIVE,
                        USER_ID,
                        "Ali Khan",
                        EMAIL,
                        new BigDecimal("5000.00")
                );
        Page<AdminAccountSummaryResponse> page = new PageImpl<>(
                List.of(account),
                PageRequest.of(2, 5),
                11
        );
        when(accountService.getAccounts(
                eq("Ali"),
                eq(AccountStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/accounts")
                        .param("search", "Ali")
                        .param("status", "ACTIVE")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountNumber").value(
                        ACCOUNT_NUMBER
                ))
                .andExpect(jsonPath("$.content[0].holderName").value(
                        "Ali Khan"
                ))
                .andExpect(jsonPath("$.content[0].holderEmail").value(EMAIL))
                .andExpect(jsonPath("$.content[0].accountStatus").value(
                        "ACTIVE"
                ));

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);
        verify(accountService).getAccounts(
                eq("Ali"),
                eq(AccountStatus.ACTIVE),
                pageableCaptor.capture()
        );
        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldReturnAccountDetails() throws Exception {
        AdminAccountDetailsResponse response =
                new AdminAccountDetailsResponse(
                        ACCOUNT_NUMBER,
                        AccountStatus.ACTIVE,
                        USER_ID,
                        "Ali Khan",
                        EMAIL,
                        "Lahore",
                        ApprovalStatus.APPROVED,
                        new BigDecimal("5000.00"),
                        BalanceIndicator.CREDIT,
                        LocalDateTime.of(2026, 8, 1, 10, 30)
                );
        when(accountService.getAccountDetails(ACCOUNT_NUMBER))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/admin/accounts/{accountNumber}",
                        ACCOUNT_NUMBER
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.holderName").value("Ali Khan"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.balanceIndicator").value("CREDIT"));
    }

    @Test
    void shouldCloseAccount() throws Exception {
        AccountClosureResponse response = new AccountClosureResponse(
                ACCOUNT_NUMBER,
                AccountStatus.CLOSED,
                BigDecimal.ZERO
        );
        when(accountService.closeAccount(ACCOUNT_NUMBER)).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/admin/accounts/{accountNumber}/close",
                        ACCOUNT_NUMBER
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.accountStatus").value("CLOSED"))
                .andExpect(jsonPath("$.finalBalance").value(0));
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts")
                        .param("status", "BLOCKED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_INVALID_PARAMETER"
                ));

        verifyNoInteractions(accountService);
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        when(accountService.getAccountDetails(ACCOUNT_NUMBER))
                .thenThrow(new ResourceNotFoundException(
                        "Bank account not found: " + ACCOUNT_NUMBER
                ));

        mockMvc.perform(get(
                        "/api/v1/admin/accounts/{accountNumber}",
                        ACCOUNT_NUMBER
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_RESOURCE_NOT_FOUND"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Bank account not found: " + ACCOUNT_NUMBER
                ));
    }
}
