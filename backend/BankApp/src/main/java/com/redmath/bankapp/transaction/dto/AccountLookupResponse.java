package com.redmath.bankapp.transaction.dto;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;

public record AccountLookupResponse(
        String accountNumber,
        String accountHolderName,
        AccountStatus status
) {
    /**
     * Factory method to map from BankAccount entity safely.
     */
    public static AccountLookupResponse fromEntity(BankAccount account) {
        if (account == null) {
            return null;
        }

        // Extract user's name if available (e.g. "Talha Ahmed" -> "Talha A.")
        String fullName = account.getUser() != null
                ? account.getUser().getName()
                : "Unknown";

        return new AccountLookupResponse(
                account.getAccountNumber(),
                fullName,
                account.getStatus()
        );
    }

    /**
     * Optional helper to mask/format name for privacy (e.g., "John Doe" -> "John D.")
     */
    private static String formatHolderName(String firstName, String lastName) {
        if (firstName == null) return "Unknown";
        if (lastName == null || lastName.isBlank()) return firstName;
        return firstName + " " + lastName.charAt(0) + ".";
    }
}