package com.redmath.bankapp.transaction.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record UserTransactionsResponse(
    List<TransactionResponse> transactions,
    int currentPage,
    int totalPages,
    long totalElements,
    boolean isLast
) {

  /**
   * Helper factory method to construct response directly from a Spring Data Page
   */
  public static UserTransactionsResponse fromPage(Page<TransactionResponse> page) {
    return new UserTransactionsResponse(
        page.getContent(),
        page.getNumber(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.isLast()
    );
  }
}