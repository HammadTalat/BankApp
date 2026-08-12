package com.redmath.redmathbankmcp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserTransactionsResponse(

        List<TransactionResponse> transactions,

        int currentPage,

        int totalPages,

        long totalElements,

        boolean isLast

) {
}
