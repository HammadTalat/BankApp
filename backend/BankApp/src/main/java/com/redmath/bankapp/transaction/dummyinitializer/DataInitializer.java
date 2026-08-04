//package com.redmath.bankapp.transaction.dummyinitializer;
//
//import com.redmath.bankapp.account.entity.AccountBalance;
//import com.redmath.bankapp.account.entity.BalanceIndicator;
//import com.redmath.bankapp.account.entity.BankAccount;
//import com.redmath.bankapp.account.repository.AccountBalanceRepository;
//import com.redmath.bankapp.account.repository.BankAccountRepository;
//import com.redmath.bankapp.user.entity.AppUser;
//import com.redmath.bankapp.user.entity.ApprovalStatus;
//import com.redmath.bankapp.user.entity.Role;
//import com.redmath.bankapp.user.repository.AppUserRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//import java.math.BigDecimal;
//
//@Configuration
//@Profile("dev") // Only runs in local/dev environment
//public class DataInitializer {
//
//    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
//
//    @Bean
//    public CommandLineRunner initDatabase(
//            AppUserRepository userRepository,
//            BankAccountRepository accountRepository,
//            AccountBalanceRepository balanceRepository) {
//
//        return args -> {
//            // Avoid duplicate seeding if data already exists
//            if (userRepository.count() > 1) {
//                log.info("Database already seeded. Skipping initial data load.");
//                return;
//            }
//
//            log.info("Seeding dummy users and accounts for testing...");
//
//            // ==========================================
//            // 1. Create Sender (User 1 - Linked to ID 1L for DevMockUserFilter)
//            // ==========================================
//            AppUser user1 = new AppUser();
//            user1.setId(1L);
//            user1.setEmail("sender@redmath.com");
//            user1.setName("John Doe Sender");
//            user1.setAddress("123");
//            user1.setRole(Role.ACCOUNT_HOLDER);
//            // Set password or other mandatory fields on User entity if needed
//            user1 = userRepository.save(user1);
//            user1.setApprovalStatus(ApprovalStatus.APPROVED);
//
//            BankAccount account1 = new BankAccount();
//            account1.setAccountNumber("PK1000000001");
//            account1.setUser(user1);
//            account1 = accountRepository.save(account1);
//
//            AccountBalance balance1 = new AccountBalance(
//                    account1,
//                    new BigDecimal("5000.00"), // Starting balance: $5000
//                    BalanceIndicator.CREDIT
//            );
//            balanceRepository.save(balance1);
//
//            // ==========================================
//            // 2. Create Recipient (User 2)
//            // ==========================================
//            AppUser user2 = new AppUser();
//            user2.setId(2L);
//            user2.setEmail("recipient@redmath.com");
//            user2.setName("Jane Smith Recipient");
//            user2.setAddress("123");
//            user2.setApprovalStatus(ApprovalStatus.APPROVED);
//            user2 = userRepository.save(user2);
//            user2.setRole(Role.ACCOUNT_HOLDER);
//
//            BankAccount account2 = new BankAccount();
//            account2.setAccountNumber("PK2000000002");
//            account2.setUser(user2);
//            account2 = accountRepository.save(account2);
//
//            AccountBalance balance2 = new AccountBalance(
//                    account2,
//                    new BigDecimal("1000.00"), // Starting balance: $1000
//                    BalanceIndicator.CREDIT
//            );
//            balanceRepository.save(balance2);
//
//            log.info("Database seeding completed!");
//            log.info("User 1 (Sender) Account: PK1000000001 | Balance: $5000.00");
//            log.info("User 2 (Recipient) Account: PK2000000002 | Balance: $1000.00");
//        };
//    }
//}