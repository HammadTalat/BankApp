package com.redmath.bankapp.account.entity;


import com.redmath.bankapp.user.entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
public class BankAccount {

    @Id
    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private AccountStatus status;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_bank_account_user"
            )
    )
    private AppUser user;



    public BankAccount(
            String accountNumber,
            AppUser user
    ) {
        this.accountNumber = Objects.requireNonNull(
                accountNumber,
                "Account number is required"
        );

        this.user = Objects.requireNonNull(
                user,
                "Account holder is required"
        );

        this.status = AccountStatus.ACTIVE;
    }

    public BankAccount() {

    }


    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == AccountStatus.CLOSED;
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
    }
}