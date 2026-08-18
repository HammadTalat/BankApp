package com.redmath.bankapp.transaction.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.account.entity.BankAccount;
import com.redmath.bankapp.transaction.entity.AccountTransaction;
import com.redmath.bankapp.transaction.enums.TransactionIndicator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TransactionResponseFactoryTest {

    @Test
    void mapsTransactionWithSenderAndRecipientAccountNumbers() {
        AccountTransaction transaction = transaction(account("PK100"), account("PK200"));

        TransactionResponse response = TransactionResponse.fromEntity(transaction);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.operationId()).isEqualTo("operation-12");
        assertThat(response.accountId()).isEqualTo("PK100");
        assertThat(response.recipientAccountId()).isEqualTo("PK200");
        assertThat(response.amount()).isEqualByComparingTo("250.00");
    }

    @Test
    void mapsMissingOptionalAccountReferencesAsNull() {
        TransactionResponse response = TransactionResponse.fromEntity(transaction(null, null));

        assertThat(response.accountId()).isNull();
        assertThat(response.recipientAccountId()).isNull();
    }

    @Test
    void mapsAccountLookupForNullUserAndExistingUser() {
        BankAccount withoutUser = account("PK300");
        assertThat(AccountLookupResponse.fromEntity(null)).isNull();
        assertThat(AccountLookupResponse.fromEntity(withoutUser))
                .extracting(AccountLookupResponse::accountHolderName,
                        AccountLookupResponse::accountNumber,
                        AccountLookupResponse::status)
                .containsExactly("Unknown", "PK300", AccountStatus.ACTIVE);
    }

    private AccountTransaction transaction(BankAccount account, BankAccount recipient) {
        return AccountTransaction.builder()
                .id(12L)
                .operationId("operation-12")
                .account(account)
                .recipientAccount(recipient)
                .description("Transfer")
                .amount(new BigDecimal("250.00"))
                .indicator(TransactionIndicator.DEBIT)
                .transactionDate(LocalDateTime.of(2026, 8, 18, 10, 0))
                .build();
    }

    private BankAccount account(String number) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(number);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }
}
