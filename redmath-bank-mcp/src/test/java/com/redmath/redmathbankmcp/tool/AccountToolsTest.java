package com.redmath.redmathbankmcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.AccountResponse;
import com.redmath.redmathbankmcp.dto.AccountSummaryResponse;
import com.redmath.redmathbankmcp.dto.BalanceResponse;
import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountToolsTest {

    private final BankApiClient bankApiClient = mock(BankApiClient.class);
    private final McpTransportContext transportContext = McpTransportContext.create(Map.of(
            McpTransportConfig.AUTHORIZATION_HEADER, "Bearer user-token",
            McpTransportConfig.COOKIE_HEADER, "accessToken=user-token"
    ));

    private AccountTools accountTools;

    @BeforeEach
    void setUp() {
        accountTools = new AccountTools(bankApiClient);
    }

    @Test
    void accountSummaryCombinesTheAuthenticatedUsersAccountAndBalance() {
        when(bankApiClient.getAccount("Bearer user-token", "accessToken=user-token"))
                .thenReturn(new AccountResponse("RM-1001", "ACTIVE"));
        when(bankApiClient.getBalance("Bearer user-token", "accessToken=user-token"))
                .thenReturn(new BalanceResponse(new BigDecimal("1250.50")));

        AccountSummaryResponse response = accountTools.getAccountSummary(transportContext);

        assertThat(response.accountNumber()).isEqualTo("RM-1001");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.balance()).isEqualByComparingTo("1250.50");
        verify(bankApiClient).getAccount("Bearer user-token", "accessToken=user-token");
        verify(bankApiClient).getBalance("Bearer user-token", "accessToken=user-token");
    }

    @Test
    void recentTransactionsUsesTheDefaultLimitAndLastThirtyDays() {
        LocalDate endDate = LocalDate.now();
        UserTransactionsResponse expected = new UserTransactionsResponse(List.of(), 0, 0, 0, true);
        when(bankApiClient.searchTransactions(
                "Bearer user-token", "accessToken=user-token", endDate.minusDays(30).toString(), endDate.toString(), 0, 10
        )).thenReturn(expected);

        UserTransactionsResponse response = accountTools.getRecentTransactions(null, transportContext);

        assertThat(response).isSameAs(expected);
        verify(bankApiClient).searchTransactions(
                "Bearer user-token", "accessToken=user-token", endDate.minusDays(30).toString(), endDate.toString(), 0, 10);
    }

    @Test
    void rejectsAnInvalidRecentTransactionLimit() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> accountTools.getRecentTransactions(21, transportContext))
                .withMessage("limit must be between 1 and 20.");

        verifyNoInteractions(bankApiClient);
    }
}
