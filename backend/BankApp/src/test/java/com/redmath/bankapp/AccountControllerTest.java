package com.redmath.bankapp;

import com.redmath.bankapp.account.controller.AccountController;
import com.redmath.bankapp.account.dto.AccountResponse;
import com.redmath.bankapp.account.dto.BalanceResponse;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.service.AccountService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private Jwt mockJwt;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiSecurityService apiSecurityService;

    @MockitoBean
    private AccountService accountService;


    @Test
    @DisplayName("GET /balance - Should return 200 OK and BalanceResponse JSON when authenticated")
    void getBalance_Authenticated_Returns200AndBalance() throws Exception {
        BalanceResponse balanceResponse = new BalanceResponse(new BigDecimal("1500.50"));
        given(accountService.getBalance(any(Jwt.class))).willReturn(balanceResponse);

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500.50));

        verify(accountService).getBalance(any(Jwt.class));
    }

    @Test
    @DisplayName("GET /balance - Should return 404 Not Found when AccountNotFoundException is thrown")
    void getBalance_AccountNotFound_Returns404() throws Exception {
        given(accountService.getBalance(any(Jwt.class)))
                .willThrow(new AccountNotFoundException("No bank account found for user ID: 1"));

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService).getBalance(any(Jwt.class));
    }

    @Test
    @DisplayName("GET /balance - Should return 404 Not Found when BalanceNotFoundException is thrown")
    void getBalance_BalanceNotFound_Returns404() throws Exception {
        given(accountService.getBalance(any(Jwt.class)))
                .willThrow(new BalanceNotFoundException("Balance record not found for account: ACC123456"));

        mockMvc.perform(get("/api/v1/account/balance")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService).getBalance(any(Jwt.class));
    }

    @Test
    @DisplayName("GET /balance - Should return Redirection when unauthenticated")
    void getBalance_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/account/balance")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Get account should return 200 OK when authenticated")
    void getAccount_Authenticated_Returns200() throws Exception {
        AccountResponse accountResponse = new AccountResponse("1212121212", AccountStatus.ACTIVE);
        given(accountService.getAccount(any(Jwt.class))).willReturn(accountResponse);
        mockMvc.perform(get("/api/v1/account")
                        .with(jwt())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get account should return redirection when not authenticated")
    void getAccount_NotAuthenticated_Returns300() throws Exception {
        AccountResponse accountResponse = new AccountResponse("1212121212", AccountStatus.ACTIVE);
        given(accountService.getAccount(any(Jwt.class))).willReturn(accountResponse);
        mockMvc.perform(get("/api/v1/account")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }
}