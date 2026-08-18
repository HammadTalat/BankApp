package com.redmath.bankapp;

import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BalanceIndicator;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import org.junit.jupiter.api.Disabled;
import org.springframework.security.oauth2.jwt.Jwt;
import com.redmath.bankapp.transaction.dto.TransferRequest;
import com.redmath.bankapp.transaction.exception.InsufficientBalanceException;
import com.redmath.bankapp.transaction.repository.AccountTransactionRepository;
import com.redmath.bankapp.transaction.service.TransactionService;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
class TransactionServiceConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountBalanceRepository balanceRepository;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AccountTransactionRepository transactionRepository;


    private Jwt jwt1;
    private Jwt jwt2;

    @BeforeEach
    void setUpDatabase() {

        // 1. Clean up existing test data
        transactionRepository.deleteAllInBatch();
        balanceRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();


        // 2. Create Users
        AppUser user1 = new AppUser();
        user1.setApprovalStatus(ApprovalStatus.APPROVED);
        user1.setAddress("123 park");
        user1.setRole(Role.ACCOUNT_HOLDER);
        user1.setName("Test User 1");
        user1.setEmail("testuser1@redmath.com");

        AppUser user2 = new AppUser();
        user2.setApprovalStatus(ApprovalStatus.APPROVED);
        user2.setAddress("123 park");
        user2.setRole(Role.ACCOUNT_HOLDER);
        user2.setName("Test User 2");
        user2.setEmail("testuser2@redmath.com");

        userRepository.saveAndFlush(user1);
        userRepository.saveAndFlush(user2);

        jwt1 = Jwt.withTokenValue("token1").header("alg", "none").claim("userId", user1.getId()).claim("sub", user1.getEmail()).build();
        jwt2 = Jwt.withTokenValue("token2").header("alg", "none").claim("userId", user2.getId()).claim("sub", user2.getEmail()).build();


        // 3. Create Accounts
        BankAccount senderAccount = new BankAccount();
        String senderAcc = "PK1000000001";
        senderAccount.setAccountNumber(senderAcc);
        senderAccount.setUser(user1);
        senderAccount.setStatus(AccountStatus.ACTIVE);

        BankAccount receiverAccount = new BankAccount();
        String receiverAcc = "PK2000000002";
        receiverAccount.setAccountNumber(receiverAcc);
        receiverAccount.setUser(user2);
        receiverAccount.setStatus(AccountStatus.ACTIVE);


        // 5. Save bank accounts
        BankAccount senderBankAccount = accountRepository.saveAndFlush(senderAccount);
        BankAccount receiverBankAccount = accountRepository.saveAndFlush(receiverAccount);

        // 6. Set up starting balances ($1,000.00 for sender, $500.00 for receiver)
        AccountBalance initialSenderBalance = new AccountBalance(
                senderBankAccount,
                new BigDecimal("1000.00"),
                BalanceIndicator.CREDIT
        );
        AccountBalance initialReceiverBalance = new AccountBalance(
                receiverBankAccount,
                new BigDecimal("500.00"),
                BalanceIndicator.CREDIT
        );

        // 7. Persist starting state to DB so concurrent threads can read it
        balanceRepository.saveAndFlush(initialSenderBalance);
        balanceRepository.saveAndFlush(initialReceiverBalance);
    }


    @Test
    @DisplayName("Concurrent Transfers: Prevent lost updates when 10 threads transfer from the same account simultaneously")
    void executeTransfer_ConcurrentTransfers_PreventsLostUpdates() throws InterruptedException {
        // Given
        int threadCount = 10;
        BigDecimal transferAmount = new BigDecimal("100.00");
        String senderAcc = "PK1000000001"; // Initial Balance: $1,000.00
        String receiverAcc = "PK2000000002"; // Initial Balance: $500.00

        TransferRequest request = new TransferRequest(senderAcc, receiverAcc, transferAmount, "Concurrent Transfer");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount); // Synchronizes thread start
        CountDownLatch startLatch = new CountDownLatch(1);          // Master release trigger
        CountDownLatch finishLatch = new CountDownLatch(threadCount); // Tracks completion

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown(); // Signal thread is ready
                try {
                    startLatch.await(); // Wait for green light
                    transactionService.executeTransfer(jwt1, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await(); // Wait until all 10 threads are spawned and waiting
        startLatch.countDown(); // Trigger all threads at the EXACT same instant
        finishLatch.await(10, TimeUnit.SECONDS); // Block main test thread until all finish
        executor.shutdown();

        // Then
        AccountBalance updatedSenderBalance = balanceRepository.findLatestBalance(senderAcc).orElseThrow();
        AccountBalance updatedReceiverBalance = balanceRepository.findLatestBalance(receiverAcc).orElseThrow();

        // 10 successful transfers of $100.00 from $1,000.00 -> Final Balance must be $0.00
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(0);
        assertThat(updatedSenderBalance.getAmount()).isEqualByComparingTo("0.00");
        assertThat(updatedReceiverBalance.getAmount()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Concurrent Overdraft: Only allowable requests succeed when total requested exceeds balance")
    void executeTransfer_ConcurrentOverdraft_FailsGracefully() throws InterruptedException {
        int threadCount = 10;
        BigDecimal transferAmount = new BigDecimal("200.00"); // 10 * 200 = 2000 (Account only has 1000)
        String senderAcc = "PK1000000001";
        String receiverAcc = "PK2000000002";

        TransferRequest request = new TransferRequest(senderAcc, receiverAcc, transferAmount, "Overdraft Attempt");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    transactionService.executeTransfer(jwt1, request);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    t.printStackTrace();
                    exceptions.add(t);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Fire all
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert exact concurrency protection
        assertThat(successCount.get()).isEqualTo(5); // Only 5 transfers of $200 can fit in $1000 balance
        assertThat(exceptions).hasSize(5)
                .allMatch(t -> t instanceof InsufficientBalanceException);

        AccountBalance finalBalance = balanceRepository.findLatestBalance(senderAcc).orElseThrow();
        assertThat(finalBalance.getAmount()).isEqualByComparingTo("0.00");
    }


    @Test
    @DisplayName("Deadlock Prevention: Concurrent cross-transfers should complete without DB deadlock exceptions")
    void executeTransfer_CrossTransfers_DoesNotDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        BigDecimal transferAmount = new BigDecimal("200.00"); // 10 * 200 = 2000 (Account only has 1000)
        String senderAcc = "PK1000000001";
        String receiverAcc = "PK2000000002";
        TransferRequest req1 = new TransferRequest(senderAcc, receiverAcc, transferAmount, "Overdraft Attempt");
        TransferRequest req2 = new TransferRequest(receiverAcc, senderAcc, transferAmount, "Overdraft Attempt");


        Future<?> future1 = executor.submit(() -> {
            startLatch.await();
            return transactionService.executeTransfer(jwt1, req1);
        });

        Future<?> future2 = executor.submit(() -> {
            startLatch.await();
            return transactionService.executeTransfer(jwt2, req2);
        });

        startLatch.countDown(); // Launch both at once

        // If deadlock prevention works, both futures complete cleanly without throwing CannotAcquireLockException
        future1.get(5, TimeUnit.SECONDS);
        future2.get(5, TimeUnit.SECONDS);

        executor.shutdown();
    }
}