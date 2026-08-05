package com.redmath.bankapp;

import com.redmath.bankapp.account.entity.AccountStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import com.redmath.bankapp.transaction.controller.TransactionController;
import com.redmath.bankapp.transaction.dto.AccountLookupResponse;
import com.redmath.bankapp.transaction.dto.TransferRequest;
import com.redmath.bankapp.transaction.dto.TransferResponse;
import com.redmath.bankapp.transaction.dto.UserTransactionsResponse;
import com.redmath.bankapp.transaction.enums.OperationStatus;
import com.redmath.bankapp.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.redmath.bankapp.auth.security.ApiSecurityService;
import com.redmath.bankapp.auth.security.PendingProfileAccessManager;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc
class TransactionControllerTest {

    private Jwt mockJwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private ApiSecurityService apiSecurityService;

    @MockitoBean
    private PendingProfileAccessManager pendingProfileAccessManager;

    @BeforeEach
    void setUp() {
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", 1L)
                .claim("sub", "user@bank.com")
                .build();
    }

    // ==========================================
    // 1. LOOKUP ACCOUNT ENDPOINT TESTS
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/transaction/lookup")
    class LookupAccountTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK and response body when account is found")
        void lookupAccount_Success() throws Exception {
            String accountId = "PK1000000001";
            AccountLookupResponse mockResponse = new AccountLookupResponse(accountId, "John Doe", AccountStatus.ACTIVE);

            given(transactionService.lookupAccount(accountId)).willReturn(mockResponse);

            mockMvc.perform(get("/api/v1/transaction/lookup")
                            .param("accountID", accountId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value(accountId))
                    .andExpect(jsonPath("$.accountHolderName").value("John Doe"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(transactionService).lookupAccount(accountId);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 Bad Request when accountID query param is missing or blank")
        void lookupAccount_MissingParam_Returns400() throws Exception {
            mockMvc.perform(get("/api/v1/transaction/lookup")
                            .param("accountID", "   ")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(transactionService);
        }

        @Test
        @WithMockUser
        @DisplayName("Should propagate AccountNotFoundException when account does not exist")
        void lookupAccount_NotFound_ThrowsException() throws Exception {
            String accountId = "NON_EXISTENT";
            given(transactionService.lookupAccount(accountId))
                    .willThrow(new AccountNotFoundException("Account not found"));

            mockMvc.perform(get("/api/v1/transaction/lookup")
                            .param("accountID", accountId))
                    .andExpect(status().isNotFound());

            verify(transactionService).lookupAccount(accountId);
        }
    }

    // ==========================================
    // 2. EXECUTE TRANSFER ENDPOINT TESTS
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/transaction/transfer")
    class ExecuteTransferTests {



        @Test
        @DisplayName("Should return 201 Created and response body when transfer is valid")
        void executeTransfer_Success() throws Exception {
            TransferRequest request = new TransferRequest(
                    "PK1000000001",
                    "PK2000000002",
                    new BigDecimal("250.00"),
                    "Rent Payment"
            );

            TransferResponse mockResponse = new TransferResponse(
                    "TXN-12345",
                    OperationStatus.COMPLETED,
                    new BigDecimal("250.00"),
                    "PK1000000001",
                    "PK2000000002",
                    "Rent Payment",
                    LocalDateTime.now()
            );

            given(transactionService.executeTransfer(any(Jwt.class), any(TransferRequest.class)))
                    .willReturn(mockResponse);

            mockMvc.perform(post("/api/v1/transaction/transfer")
                            .with(csrf())
                            .with(jwt().jwt(mockJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.operationId").value("TXN-12345"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.amount").value(250.00))
                    .andExpect(jsonPath("$.senderAccountNumber").value("PK1000000001"))
                    .andExpect(jsonPath("$.receiverAccountNumber").value("PK2000000002"));

            verify(transactionService).executeTransfer(any(Jwt.class), any(TransferRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when validation fails (e.g. invalid amount)")
        void executeTransfer_InvalidRequestBody_Returns400() throws Exception {
            // Amount is 0.00 (violates @DecimalMin("0.01"))
            TransferRequest invalidRequest = new TransferRequest(
                    "PK1000000001",
                    "PK2000000002",
                    new BigDecimal("0.00"),
                    "Invalid Transfer"
            );

            mockMvc.perform(post("/api/v1/transaction/transfer")
                            .with(csrf())
                            .with(jwt().jwt(mockJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(transactionService);
        }
    }

    // ==========================================
    // 3. GET USER TRANSACTIONS ENDPOINT TESTS
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/transaction/get-transactions")
    class GetUserTransactionsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when user is authenticated and paginated transactions are returned")
        void getUserTransactions_Authenticated_Success() throws Exception {
            UserTransactionsResponse mockResponse = new UserTransactionsResponse(Collections.emptyList(), 0, 10, 0L, true);

            given(transactionService.getUserTransactions(any(Jwt.class), any(Pageable.class)))
                    .willReturn(mockResponse);

            mockMvc.perform(get("/api/v1/transaction/get-transactions")
                            .with(jwt().jwt(mockJwt))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(transactionService).getUserTransactions(any(Jwt.class), eq(PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("Should return 302 Found redirect when Jwt is unauthenticated")
        void getUserTransactions_Unauthenticated_Returns401() throws Exception {
            // Call endpoint without passing .with(jwt(...))
            mockMvc.perform(get("/api/v1/transaction/get-transactions"))
                    .andExpect(status().isFound());

            verifyNoInteractions(transactionService);
        }
    }
}