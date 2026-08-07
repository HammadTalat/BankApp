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

}