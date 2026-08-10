package com.redmath.bankapp;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.dto.*;
import com.redmath.bankapp.transaction.exception.BusinessRuleException;
import com.redmath.bankapp.transaction.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.redmath.bankapp.transaction.controller.TransactionController;
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
import java.time.LocalDate;
import java.time.LocalDate;
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

            given(transactionService.getUserTransactions(any(Jwt.class), nullable(LocalDate.class), nullable(LocalDate.class), any(Pageable.class)))
                    .willReturn(mockResponse);

            mockMvc.perform(get("/api/v1/transaction/get-transactions")
                            .with(jwt().jwt(mockJwt))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(transactionService).getUserTransactions(any(Jwt.class),
                    nullable(LocalDate.class),
                    nullable(LocalDate.class),
                    eq(PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("Should return 302 Found redirect when Jwt is unauthenticated")
        void getUserTransactions_Unauthenticated_Returns401() throws Exception {
            // Call endpoint without passing .with(jwt(...))
            mockMvc.perform(get("/api/v1/transaction/get-transactions"))
                    .andExpect(status().isFound());

            verifyNoInteractions(transactionService);
        }


        @Test
        @DisplayName("GET /get-transactions - Should return 200 OK with valid date range parameters")
        void getUserTransactions_WithDateFilters_Returns200() throws Exception {
            LocalDate startDate = LocalDate.of(2026, 8, 1);
            LocalDate endDate = LocalDate.of(2026, 8, 4);
            UserTransactionsResponse mockResponse = new UserTransactionsResponse(Collections.emptyList(), 0, 10, 0L, true);

            when(transactionService.getUserTransactions(
                    eq(mockJwt),
                    eq(startDate),
                    eq(endDate),
                    any(Pageable.class)
            )).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/transaction/get-transactions")
                            .with(jwt().jwt(mockJwt))
                            .with(csrf())
                            .param("startDate", "2026-08-01")
                            .param("endDate", "2026-08-04")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService).getUserTransactions(
                    any(Jwt.class),
                    eq(startDate),
                    eq(endDate),
                    any(Pageable.class)
            );
        }
    }

    @Test
    @DisplayName("GET /get-transactions - Should return 400 Bad Request on invalid date format")
    void getUserTransactions_InvalidDateFormat_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/get-transactions")
                        .with(jwt().jwt(mockJwt))
                        .with(csrf())
                        .param("startDate", "01-08-2026") // Invalid ISO format
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Nested
    @DisplayName("Deposit Tests")
//    @Import(GlobalExceptionHandler.class)
    class DepositTest {

        private String validAccountNumber;
        private BigDecimal validAmount;
        private String validDescription;
        private DepositRequest validRequest;
        private DepositResponse mockResponse;

        @BeforeEach
        void setUp() {
            validAccountNumber = "ACC-12345678";
            validAmount = new BigDecimal("100.00");
            validDescription = "Cash Deposit";

            validRequest = new DepositRequest(validAccountNumber, validAmount, validDescription);

            mockResponse = new DepositResponse(
                    "op-uuid-1234",
                    OperationStatus.COMPLETED,
                    validAmount,
                    validAccountNumber,
                    new BigDecimal("500.00"),
                    validDescription,
                    LocalDateTime.of(2026, 8, 10, 10, 0)
            );
        }

        @Test
        @DisplayName("Should return 201 Created and DepositResponse when payload is valid")
        void executeDeposit_ValidRequest_Returns201Created() throws Exception {
            given(transactionService.executeDeposit(any(Jwt.class), eq(validRequest)))
                    .willReturn(mockResponse);

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt()) // Simulates valid Jwt authentication principal
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.operationId").value("op-uuid-1234"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.accountNumber").value(validAccountNumber))
                    .andExpect(jsonPath("$.newBalance").value(500.00))
                    .andExpect(jsonPath("$.description").value(validDescription))
                    .andExpect(jsonPath("$.transactionDate").exists());

            verify(transactionService).executeDeposit(any(Jwt.class), eq(validRequest));
        }

        @Test
        @DisplayName("Should return 201 Created when optional description is null")
        void executeDeposit_NullDescription_Returns201Created() throws Exception {
            DepositRequest requestWithNullDesc = new DepositRequest(validAccountNumber, validAmount, null);

            given(transactionService.executeDeposit(any(Jwt.class), eq(requestWithNullDesc)))
                    .willReturn(mockResponse);

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithNullDesc)))
                    .andExpect(status().isCreated());

            verify(transactionService).executeDeposit(any(Jwt.class), eq(requestWithNullDesc));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when accountNumber is blank")
        void executeDeposit_BlankAccountNumber_Returns400BadRequest() throws Exception {
            DepositRequest invalidRequest = new DepositRequest("", validAmount, validDescription);

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).executeDeposit(any(), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when amount is null")
        void executeDeposit_NullAmount_Returns400BadRequest() throws Exception {
            DepositRequest invalidRequest = new DepositRequest(validAccountNumber, null, validDescription);

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).executeDeposit(any(), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when amount is less than 0.01")
        void executeDeposit_AmountZeroOrNegative_Returns400BadRequest() throws Exception {
            DepositRequest invalidRequest = new DepositRequest(validAccountNumber, new BigDecimal("0.00"), validDescription);

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).executeDeposit(any(), any());
        }

        @Test
        @DisplayName("Should return 404 Not Found when account does not exist")
        void executeDeposit_AccountNotFoundException_Returns404() throws Exception {
            given(transactionService.executeDeposit(any(Jwt.class), eq(validRequest)))
                    .willThrow(new AccountNotFoundException("Account not found: " + validAccountNumber));

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound()); // Assumes @ControllerAdvice maps AccountNotFoundException to 404 NOT_FOUND

            verify(transactionService).executeDeposit(any(Jwt.class), eq(validRequest));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when business rule fails in service")
        void executeDeposit_BusinessRuleException_Returns400() throws Exception {
            given(transactionService.executeDeposit(any(Jwt.class), eq(validRequest)))
                    .willThrow(new BusinessRuleException("Account is inactive"));

            mockMvc.perform(post("/api/v1/transaction/deposit")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest()); // Assumes @ControllerAdvice maps BusinessRuleException to 400 BAD_REQUEST

            verify(transactionService).executeDeposit(any(Jwt.class), eq(validRequest));
        }
    }

}
