package com.redmath.bankapp.admin.service;


import com.redmath.bankapp.account.entity.AccountBalance;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.account.repository.AccountBalanceRepository;
import com.redmath.bankapp.account.repository.BankAccountRepository;
import com.redmath.bankapp.admin.dto.response.UserApprovalResponse;
import com.redmath.bankapp.admin.exception.InvalidUserStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private static final int RANDOM_PART_LENGTH = 16;

    private final AppUserRepository appUserRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountBalanceRepository accountBalanceRepository;

    @Transactional
    public UserApprovalResponse approveUser(Long userId) {
        AppUser user = findUser(userId);

        validateUserForApproval(user);

        String accountNumber = generateUniqueAccountNumber();

        BankAccount account = new BankAccount(
                accountNumber,
                user
        );

        AccountBalance balance = new AccountBalance(account);

        user.setApprovalStatus(ApprovalStatus.APPROVED);

        appUserRepository.save(user);
        bankAccountRepository.save(account);
        accountBalanceRepository.save(balance);

        return toResponse(user, account, balance);
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId
                ));
    }

    private void validateUserForApproval(AppUser user) {
        if (user.getRole() != Role.ACCOUNT_HOLDER) {
            throw new InvalidUserStateException(
                    "Only account holders can be approved"
            );
        }

        if (user.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new InvalidUserStateException(
                    "Only pending users can be approved"
            );
        }

        if (user.getAddress() == null || user.getAddress().isBlank()) {
            throw new InvalidUserStateException(
                    "User address is required before approval"
            );
        }

        if (bankAccountRepository.existsByUser_Id(user.getId())) {
            throw new InvalidUserStateException(
                    "The user already has a bank account"
            );
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            accountNumber = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, RANDOM_PART_LENGTH)
                    .toUpperCase();

        } while (bankAccountRepository.existsById(accountNumber));

        return accountNumber;
    }

    private UserApprovalResponse toResponse(
            AppUser user,
            BankAccount account,
            AccountBalance balance
    ) {
        return new UserApprovalResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getApprovalStatus(),
                account.getAccountNumber(),
                account.getStatus(),
                balance.getAmount()
        );
    }
}