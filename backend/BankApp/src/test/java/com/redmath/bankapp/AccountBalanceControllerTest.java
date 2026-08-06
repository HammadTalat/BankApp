package com.redmath.bankapp;

import com.redmath.bankapp.account.controller.AccountBalanceController;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.service.AccountBalanceService;
import com.redmath.bankapp.auth.security.ApiSecurityService;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.exception.BalanceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountBalanceController.class)
class AccountBalanceControllerTest {

    private Jwt mockJwt;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiSecurityService apiSecurityService;

    @MockitoBean
    private AccountBalanceService accountBalanceService;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "test@redmath.com", "pass", Collections.emptyList());
    }

    @Test
    @DisplayName("GET /balance - Should return 200 OK and BalanceResponse JSON when authenticated")
    void getBalance_Authenticated_Returns200AndBalance() throws Exception {
        BalanceResponse balanceResponse = new BalanceResponse(new BigDecimal("1500.50"));
        given(accountBalanceService.getBalance(any(UserPrincipal.class))).willReturn(balanceResponse);

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(user(userPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500.50));

        verify(accountBalanceService).getBalance(any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /balance - Should return 404 Not Found when AccountNotFoundException is thrown")
    void getBalance_AccountNotFound_Returns404() throws Exception {
        given(accountBalanceService.getBalance(any(UserPrincipal.class)))
                .willThrow(new AccountNotFoundException("No bank account found for user ID: 1"));

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(user(userPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountBalanceService).getBalance(any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /balance - Should return 404 Not Found when BalanceNotFoundException is thrown")
    void getBalance_BalanceNotFound_Returns404() throws Exception {
        given(accountBalanceService.getBalance(any(UserPrincipal.class)))
                .willThrow(new BalanceNotFoundException("Balance record not found for account: ACC123456"));

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(user(userPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountBalanceService).getBalance(any(UserPrincipal.class));
    }

    @Test
    @DisplayName("GET /balance - Should return 401 Unauthorized when unauthenticated")
    void getBalance_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/balance")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }
}