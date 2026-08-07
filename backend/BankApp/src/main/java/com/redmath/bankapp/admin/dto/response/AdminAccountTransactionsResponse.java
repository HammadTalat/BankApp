package com.redmath.bankapp.admin.dto.response;

import com.redmath.bankapp.transaction.dto.TransactionResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record AdminAccountTransactionsResponse(
        List<TransactionResponse> transactions,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean isLast
) {

    public static AdminAccountTransactionsResponse fromPage(
            Page<TransactionResponse> page
    ) {
        return new AdminAccountTransactionsResponse(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isLast()
        );
    }
}