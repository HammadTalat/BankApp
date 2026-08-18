package com.redmath.bankapp;

import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.riskservice.dto.EvaluateRiskRequest;
import com.redmath.bankapp.riskservice.dto.EvaluateRiskResponse;
import com.redmath.bankapp.riskservice.service.RiskEvaluatorClient;
import com.redmath.bankapp.tempconfig.security.UserPrincipal;
import com.redmath.bankapp.transaction.dto.DepositRequest;
import com.redmath.bankapp.transaction.dto.DepositResponse;
import com.redmath.bankapp.transaction.exception.UnauthorizedAccessException;
import org.springframework.security.oauth2.jwt.Jwt;
import com.redmath.bankapp.transaction.dto.TransferRequest;
import com.redmath.bankapp.transaction.dto.TransferResponse;
import com.redmath.bankapp.transaction.entity.*;
import javax.security.auth.login.AccountNotFoundException;

import com.redmath.bankapp.transaction.enums.OperationStatus;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;
import com.redmath.bankapp.transaction.exception.BusinessRuleException;
import com.redmath.bankapp.transaction.exception.InsufficientBalanceException;
import com.redmath.bankapp.transaction.repository.AccountTransactionRepository;
import com.redmath.bankapp.transaction.service.TransactionService;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository balanceRepository;

    @Mock
    private AccountTransactionRepository transactionRepository;

    @Mock
    private RiskEvaluatorClient riskEvaluatorClient;

    @InjectMocks
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<AccountBalance> balanceCaptor;

    @Captor
    private ArgumentCaptor<AccountTransaction> transactionCaptor;

    private UserPrincipal userPrincipal;
    private Jwt jwt;
    private BankAccount senderAccount;
    private BankAccount receiverAccount;
    private AccountBalance senderBalance;
    private AccountBalance receiverBalance;
    private static final String SENDER_ACC = "PK1000000001";
    private static final String RECEIVER_ACC = "PK2000000002";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", USER_ID)
                .claim("sub", "test@redmath.com")
                .build();

        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setApprovalStatus(ApprovalStatus.APPROVED);
        user1.setAddress("123 park");
        user1.setRole(Role.ACCOUNT_HOLDER);
        user1.setName("Test User 1");
        user1.setEmail("testuser1@redmath.com");

        AppUser user2 = new AppUser();
        user2.setId(2L);
        user2.setApprovalStatus(ApprovalStatus.APPROVED);
        user2.setAddress("123 park");
        user2.setRole(Role.ACCOUNT_HOLDER);
        user2.setName("Test User 2");
        user2.setEmail("testuser2@redmath.com");


        senderAccount = new BankAccount();
        senderAccount.setAccountNumber(SENDER_ACC);
        senderAccount.setUser(user1);
        senderAccount.setStatus(AccountStatus.ACTIVE);

        receiverAccount = new BankAccount();
        receiverAccount.setAccountNumber(RECEIVER_ACC);
        receiverAccount.setUser(user2);
        receiverAccount.setStatus(AccountStatus.ACTIVE);

        senderBalance = new AccountBalance(senderAccount, new BigDecimal("1000.00"), BalanceIndicator.CREDIT);
        receiverBalance = new AccountBalance(receiverAccount, new BigDecimal("500.00"), BalanceIndicator.CREDIT);
    }


    @Nested
    @DisplayName("1. Validation & Precondition Tests")
    class PreconditionTests {

//        @Test
//        void getUserTransactions_ConvertsLocalDateToLocalDateTimeBoundary() throws Exception {
//            LocalDate startDate = LocalDate.of(2026, 8, 1);
//            LocalDate endDate = LocalDate.of(2026, 8, 4);
//            Pageable pageable = PageRequest.of(0, 10);
//
//            LocalDateTime expectedStart = startDate.atStartOfDay();
//            LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);
//
//            given(accountRepository.findByUser_Id(userPrincipal.getId())).willReturn(Optional.of(senderAccount));
//
//            Page<AccountTransaction> emptyPage = new PageImpl<>(Collections.emptyList());
//
//            when(transactionRepository.findByAccountNumberAndDateRange(
//                    eq("PK1000000001"), eq(expectedStart), eq(expectedEnd), eq(pageable)
//            )).thenReturn(emptyPage);
//
//            transactionService.getUserTransactions(userPrincipal, startDate, endDate, pageable);
//
//            verify(transactionRepository).findByAccountNumberAndDateRange(
//                    eq("PK1000000001"), eq(expectedStart), eq(expectedEnd), eq(pageable)
//            );
//        }

        @Test
        void getUserTransactions_ConvertsLocalDateToLocalDateTimeBoundary() throws Exception {
            LocalDate startDate = LocalDate.of(2026, 8, 1);
            LocalDate endDate = LocalDate.of(2026, 8, 4);
            Pageable pageable = PageRequest.of(0, 10);

            LocalDateTime expectedStart = startDate.atStartOfDay();
            LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);

            given(accountRepository.findByUser_Id(any(Long.class))).willReturn(Optional.of(senderAccount));

            Page<AccountTransaction> emptyPage = new PageImpl<>(Collections.emptyList());

            when(transactionRepository.findByAccountNumberAndDateRange(
                    eq("PK1000000001"), eq(expectedStart), eq(expectedEnd), eq(pageable)
            )).thenReturn(emptyPage);

            transactionService.getUserTransactions(jwt, startDate, endDate, pageable);

            verify(transactionRepository).findByAccountNumberAndDateRange(
                    eq("PK1000000001"), eq(expectedStart), eq(expectedEnd), eq(pageable)
            );
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when sender and receiver account numbers match (Case-Insensitive)")
        void executeTransfer_SelfTransfer_ThrowsException() {
            TransferRequest request = new TransferRequest(SENDER_ACC, SENDER_ACC.toLowerCase(), new BigDecimal("100.00"), "Self Transfer");

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Sender and receiver accounts cannot be the same");

            verifyNoInteractions(accountRepository, balanceRepository, transactionRepository);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException when sender account does not exist")
        void executeTransfer_SenderAccountNotFound_ThrowsException() {
            TransferRequest request = new TransferRequest("INVALID_SENDER", RECEIVER_ACC, new BigDecimal("100.00"), "Transfer");
            given(accountRepository.findByIdForUpdate("INVALID_SENDER")).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessage("Account not found: INVALID_SENDER");

            verify(accountRepository, times(1)).findByIdForUpdate("INVALID_SENDER");
            verifyNoMoreInteractions(accountRepository);
            verifyNoInteractions(balanceRepository, transactionRepository);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException when sender account does not exist")
        void executeTransfer_SenderAccount2NotFound_ThrowsException() {
            // Arrange
            String nonExistentSender = "INVALID_SENDER";
            TransferRequest request = new TransferRequest(RECEIVER_ACC, nonExistentSender, new BigDecimal("100.00"), "Transfer");

            given(accountRepository.findByIdForUpdate(nonExistentSender)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository).findByIdForUpdate(nonExistentSender);
            verifyNoMoreInteractions(accountRepository);
            verifyNoInteractions(balanceRepository, transactionRepository);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException when receiver account does not exist")
        void executeTransfer_ReceiverAccountNotFound_ThrowsException() {
            TransferRequest request = new TransferRequest(SENDER_ACC, "INVALID_RECEIVER", new BigDecimal("100.00"), "Transfer");

            given(accountRepository.findByIdForUpdate("INVALID_RECEIVER")).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessage("Account not found: INVALID_RECEIVER");

            verify(accountRepository, times(1)).findByIdForUpdate("INVALID_RECEIVER");
            verifyNoInteractions(balanceRepository, transactionRepository);
        }

        @Test
        @DisplayName("Should process transfer using zero balance when sender balance record is missing in DB")
        void executeTransfer_SenderBalanceMissing_UsesZeroBalance() {
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, new BigDecimal("100.00"), "Transfer");

            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.empty());

            // Assuming a balance of 0 leads to an insufficient funds exception during transfer validation:
            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance to perform this transfer");

            verify(balanceRepository, times(1)).findLatestBalanceForUpdate(SENDER_ACC);
        }

        @Test
        @DisplayName("Should successfully execute transfer when receiver balance record is missing in DB")
        void executeTransfer_ReceiverBalanceMissing_ProceedsSuccessfully() throws AccountNotFoundException {
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, new BigDecimal("100.00"), "Transfer");

            EvaluateRiskResponse riskResponse = new EvaluateRiskResponse(true, "Valid", "Valid");
            given(transactionRepository.findTop20ByAccount_AccountNumberOrderByTransactionDateDesc(SENDER_ACC))
                    .willReturn(List.of());
            given(riskEvaluatorClient.evaluateTransactionRisk(any(EvaluateRiskRequest.class))).willReturn(riskResponse);
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(senderBalance));
            given(balanceRepository.findLatestBalanceForUpdate(RECEIVER_ACC)).willReturn(Optional.empty());

            // Execute transfer - should not throw an exception
            TransferResponse response = transactionService.executeTransfer(jwt, request);

            // Assert successful completion
            assertThat(response).isNotNull();

            // Verify interaction and persistence steps
            verify(balanceRepository, times(1)).findLatestBalanceForUpdate(SENDER_ACC);
            verify(balanceRepository, times(1)).findLatestBalanceForUpdate(RECEIVER_ACC);
            verify(balanceRepository, atLeastOnce()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Insufficient Balance & Boundary Conditions")
    class BalanceVerificationTests {

        @Test
        @DisplayName("Should throw InsufficientBalanceException when transfer amount exceeds balance by 0.01")
        void executeTransfer_AmountGreaterThanBalance_ThrowsException() {
            BigDecimal transferAmount = new BigDecimal("1000.01"); // Balance is 1000.00
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, transferAmount, "Overdraft Attempt");

            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(senderBalance));

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessage("Insufficient balance to perform this transfer");

            verify(balanceRepository, never()).findLatestBalanceForUpdate(RECEIVER_ACC);
            verify(balanceRepository, never()).save(any());
            verifyNoInteractions(transactionRepository);
        }

        @Test
        @DisplayName("Should succeed when transfer amount exactly equals sender balance (Zero Remaining Balance)")
        void executeTransfer_ExactBalance_Success() throws Exception {
            BigDecimal transferAmount = new BigDecimal("1000.00"); // Exact balance
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, transferAmount, "Clear Account");

            EvaluateRiskResponse riskResponse = new EvaluateRiskResponse(true, "Valid", "Valid");
            given(transactionRepository.findTop20ByAccount_AccountNumberOrderByTransactionDateDesc(SENDER_ACC))
                    .willReturn(List.of());
            given(riskEvaluatorClient.evaluateTransactionRisk(any(EvaluateRiskRequest.class))).willReturn(riskResponse);
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(senderBalance));
            given(balanceRepository.findLatestBalanceForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverBalance));


            TransferResponse response = transactionService.executeTransfer(jwt, request);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(OperationStatus.COMPLETED);

            verify(balanceRepository, times(2)).save(balanceCaptor.capture());
            List<AccountBalance> savedBalances = balanceCaptor.getAllValues();

            // Assert sender new balance is exactly 0.00
            assertThat(savedBalances.get(0).getAmount()).isEqualByComparingTo("0.00");
            assertThat(savedBalances.get(0).getIndicator()).isEqualTo(BalanceIndicator.DEBIT);

            // Assert receiver new balance is 1500.00
            assertThat(savedBalances.get(1).getAmount()).isEqualByComparingTo("1500.00");
            assertThat(savedBalances.get(1).getIndicator()).isEqualTo(BalanceIndicator.CREDIT);
        }
    }

    @Nested
    @DisplayName("3. Execution & Ledger Verification Tests")
    class LedgerExecutionTests {

        @Test
        @DisplayName("Should successfully process transfer, persist correct balance entries and double-entry transactions")
        void executeTransfer_Success() throws Exception {
            BigDecimal transferAmount = new BigDecimal("250.00");
            String description = "Rent Payment";
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, transferAmount, description);

            EvaluateRiskResponse riskResponse = new EvaluateRiskResponse(true, "Valid", "Valid");
            given(transactionRepository.findTop20ByAccount_AccountNumberOrderByTransactionDateDesc(SENDER_ACC))
                    .willReturn(List.of());
            given(riskEvaluatorClient.evaluateTransactionRisk(any(EvaluateRiskRequest.class))).willReturn(riskResponse);
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(senderBalance));
            given(balanceRepository.findLatestBalanceForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverBalance));

            // Execute
            TransferResponse response = transactionService.executeTransfer(jwt, request);

            // 1. Assert Response Integrity (Field-by-Field for PITest)
            assertThat(response).isNotNull();
            assertThat(response.operationId()).isNotNull().isNotBlank();
            assertThat(response.status()).isEqualTo(OperationStatus.COMPLETED);
            assertThat(response.amount()).isEqualByComparingTo("250.00");
            assertThat(response.senderAccountNumber()).isEqualTo(SENDER_ACC);
            assertThat(response.receiverAccountNumber()).isEqualTo(RECEIVER_ACC);
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.timestamp()).isNotNull();

            // 2. Assert Balance Updates
            verify(balanceRepository, times(2)).save(balanceCaptor.capture());
            List<AccountBalance> savedBalances = balanceCaptor.getAllValues();

            AccountBalance senderNewBalance = savedBalances.get(0);
            assertThat(senderNewBalance.getAccount().getAccountNumber()).isEqualTo(SENDER_ACC);
            assertThat(senderNewBalance.getAmount()).isEqualByComparingTo("750.00"); // 1000 - 250
            assertThat(senderNewBalance.getIndicator()).isEqualTo(BalanceIndicator.DEBIT);

            AccountBalance receiverNewBalance = savedBalances.get(1);
            assertThat(receiverNewBalance.getAccount().getAccountNumber()).isEqualTo(RECEIVER_ACC);
            assertThat(receiverNewBalance.getAmount()).isEqualByComparingTo("750.00"); // 500 + 250
            assertThat(receiverNewBalance.getIndicator()).isEqualTo(BalanceIndicator.CREDIT);

            // 3. Assert Double-Entry Ledger Transactions
            verify(transactionRepository, times(2)).save(transactionCaptor.capture());
            List<AccountTransaction> savedTransactions = transactionCaptor.getAllValues();

            AccountTransaction debitTxn = savedTransactions.get(0);
            assertThat(debitTxn.getAccount()).isEqualTo(senderAccount);
            assertThat(debitTxn.getRecipientAccount()).isEqualTo(receiverAccount);
            assertThat(debitTxn.getAmount()).isEqualByComparingTo("250.00");
            assertThat(debitTxn.getIndicator()).isEqualTo(TransactionIndicator.DEBIT);
            assertThat(debitTxn.getDescription()).isEqualTo(description);
            assertThat(debitTxn.getOperationId()).isEqualTo(response.operationId());

            AccountTransaction creditTxn = savedTransactions.get(1);
            assertThat(creditTxn.getAccount()).isEqualTo(receiverAccount);
            assertThat(creditTxn.getRecipientAccount()).isEqualTo(senderAccount);
            assertThat(creditTxn.getAmount()).isEqualByComparingTo("250.00");
            assertThat(creditTxn.getIndicator()).isEqualTo(TransactionIndicator.CREDIT);
            assertThat(creditTxn.getDescription()).isEqualTo(description);
            assertThat(creditTxn.getOperationId()).isEqualTo(response.operationId());
        }
    }

    @Nested
    @DisplayName("Deposit Execution - Success Cases")
    class SuccessCases {


        private final Long userId = 99L;
        private final String accountNumber = "ACC-12345";
        private BankAccount targetAccount;

        @BeforeEach
        void setUp() {
            targetAccount = new BankAccount();
            targetAccount.setAccountNumber(accountNumber);
            // Assuming your account entity tracks ownership via a user object or userId field
            // Configure standard valid state for targetAccount to pass validateAccountOwnership & validateAccountActive
        }

        @Test
        @DisplayName("Should execute deposit when account has existing balance and description is provided")
        void executeDeposit_ExistingBalanceAndDescription_Success() throws Exception {
            DepositRequest request = new DepositRequest(SENDER_ACC, new BigDecimal("100.00"), "Monthly Salary");
            AccountBalance existingBalance = new AccountBalance(senderAccount, new BigDecimal("250.00"), BalanceIndicator.CREDIT);

            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(existingBalance));

            LocalDateTime beforeExecution = LocalDateTime.now().minusSeconds(1);
            DepositResponse response = transactionService.executeDeposit(jwt, request);

            // Response Assertions (Kills return object/mutation bugs)
            assertThat(response).isNotNull();
            assertThat(response.operationId()).isNotBlank();
            assertThat(response.status()).isEqualTo(OperationStatus.COMPLETED);
            assertThat(response.amount()).isEqualByComparingTo("100.00");
            assertThat(response.accountNumber()).isEqualTo(SENDER_ACC);
            assertThat(response.newBalance()).isEqualByComparingTo("350.00"); // 250 + 100
            assertThat(response.description()).isEqualTo("Monthly Salary");
            assertThat(response.transactionDate()).isAfterOrEqualTo(beforeExecution);

            // Ledger Entity Assertions (Kills persistence mutations)
            verify(balanceRepository).save(balanceCaptor.capture());
            AccountBalance savedBalance = balanceCaptor.getValue();
            assertThat(savedBalance.getAmount()).isEqualByComparingTo("350.00");
            assertThat(savedBalance.getIndicator()).isEqualTo(BalanceIndicator.CREDIT);

            verify(transactionRepository).save(transactionCaptor.capture());
            AccountTransaction savedTx = transactionCaptor.getValue();
            assertThat(savedTx.getAmount()).isEqualByComparingTo("100.00");
            assertThat(savedTx.getDescription()).isEqualTo("Monthly Salary");
        }

        @Test
        @DisplayName("Should default to zero balance and 'Cash Deposit' description when optional values are null")
        void executeDeposit_NullBalanceRecordAndNullDescription_DefaultsApplied() throws Exception {
            DepositRequest request = new DepositRequest(SENDER_ACC, new BigDecimal("50.00"), null);

            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.empty());

            DepositResponse response = transactionService.executeDeposit(jwt, request);

            assertThat(response.newBalance()).isEqualByComparingTo("50.00"); // 0 + 50
            assertThat(response.description()).isEqualTo("Cash Deposit");

            verify(balanceRepository).save(balanceCaptor.capture());
            assertThat(balanceCaptor.getValue().getAmount()).isEqualByComparingTo("50.00");

            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getDescription()).isEqualTo("Cash Deposit");
        }

        @Test
        @DisplayName("Should handle existing balance entity having a null amount field by treating it as ZERO")
        void executeDeposit_BalanceEntityWithNullAmount_TreatsAsZero() throws Exception {
            DepositRequest request = new DepositRequest(SENDER_ACC, new BigDecimal("75.00"), "Bonus");
            AccountBalance balanceWithNullAmount = new AccountBalance(senderAccount, null, BalanceIndicator.CREDIT);

            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(balanceRepository.findLatestBalanceForUpdate(SENDER_ACC)).willReturn(Optional.of(balanceWithNullAmount));

            DepositResponse response = transactionService.executeDeposit(jwt, request);

            assertThat(response.newBalance()).isEqualByComparingTo("75.00");
            verify(balanceRepository).save(balanceCaptor.capture());
            assertThat(balanceCaptor.getValue().getAmount()).isEqualByComparingTo("75.00");
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when deposit amount is null")
        void executeDeposit_NullAmount_ThrowsBusinessRuleException() {
            DepositRequest request = new DepositRequest(SENDER_ACC, null, "Deposit");

            assertThatThrownBy(() -> transactionService.executeDeposit(jwt, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Transfer amount must be greater than zero");

            verify(accountRepository, never()).findByIdForUpdate(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when deposit amount is zero")
        void executeDeposit_ZeroAmount_ThrowsBusinessRuleException() {
            DepositRequest request = new DepositRequest(SENDER_ACC, BigDecimal.ZERO, "Deposit");

            assertThatThrownBy(() -> transactionService.executeDeposit(jwt, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Transfer amount must be greater than zero");

            verify(accountRepository, never()).findByIdForUpdate(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when deposit amount is negative")
        void executeDeposit_NegativeAmount_ThrowsBusinessRuleException() {
            DepositRequest request = new DepositRequest(SENDER_ACC, new BigDecimal("-10.00"), "Deposit");

            assertThatThrownBy(() -> transactionService.executeDeposit(jwt, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Transfer amount must be greater than zero");

            verify(accountRepository, never()).findByIdForUpdate(any());
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException with exact message when account does not exist")
        void executeDeposit_AccountNotFound_ThrowsAccountNotFoundException() {
            DepositRequest request = new DepositRequest(SENDER_ACC, new BigDecimal("100.00"), "Deposit");
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.executeDeposit(jwt, request))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessage("Account not found: " + SENDER_ACC);

            verify(balanceRepository, never()).findLatestBalanceForUpdate(any());
            verify(balanceRepository, never()).save(any());
        }
    }

    @Nested
    class AuthorizationCase {

        private final Long userId = 99L;
        private final String accountNumber = "ACC-12345";
        private BankAccount targetAccount;

        @BeforeEach
        void setUp() {
            targetAccount = new BankAccount();
            targetAccount.setAccountNumber(accountNumber);
            // Assuming your account entity tracks ownership via a user object or userId field
            // Configure standard valid state for targetAccount to pass validateAccountOwnership & validateAccountActive
        }

        @Test
        @DisplayName("Should throw error when someone other than account owner is attempting transaction")
        void executeTransfer_throwsErrorWhenNotOwnerAttempts() {
            TransferRequest request = new TransferRequest(RECEIVER_ACC, SENDER_ACC, new BigDecimal("100.00"), "Transfer");

            EvaluateRiskResponse riskResponse = new EvaluateRiskResponse(true, "Valid", "Valid");
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(UnauthorizedAccessException.class);

        }

        @Test
        @DisplayName("Should throw error when someone other than account owner is attempting transaction")
        void executeTransfer_throwsErrorWhenInactiveAccount() throws AccountNotFoundException {
            BigDecimal transferAmount = new BigDecimal("250.00");
            String description = "Rent Payment";
            TransferRequest request = new TransferRequest(SENDER_ACC, RECEIVER_ACC, transferAmount, description);
            senderAccount.setStatus(AccountStatus.CLOSED);
            EvaluateRiskResponse riskResponse = new EvaluateRiskResponse(true, "Valid", "Valid");
            given(accountRepository.findByIdForUpdate(SENDER_ACC)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByIdForUpdate(RECEIVER_ACC)).willReturn(Optional.of(receiverAccount));

            assertThatThrownBy(() -> transactionService.executeTransfer(jwt, request))
                    .isInstanceOf(BusinessRuleException.class);

        }
    }
}