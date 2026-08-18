package com.redmath.redmathbankmcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionToolsTest {

    private final BankApiClient bankApiClient = mock(BankApiClient.class);
    private final McpTransportContext transportContext = McpTransportContext.create(Map.of(
            McpTransportConfig.AUTHORIZATION_HEADER, "Bearer user-token",
            McpTransportConfig.COOKIE_HEADER, "accessToken=user-token"
    ));

    private TransactionTools transactionTools;

    @BeforeEach
    void setUp() {
        transactionTools = new TransactionTools(bankApiClient);
    }

    @Test
    void usesDefaultPageAndSizeWhenTheyAreNotProvided() {
        UserTransactionsResponse expected = response();
        when(bankApiClient.searchTransactions("Bearer user-token", "accessToken=user-token", null, null, 0, 10))
                .thenReturn(expected);

        UserTransactionsResponse actual = transactionTools.searchTransactions(
                null, null, null, null, transportContext);

        assertThat(actual).isSameAs(expected);
        verify(bankApiClient).searchTransactions("Bearer user-token", "accessToken=user-token", null, null, 0, 10);
    }

    @Test
    void forwardsSuppliedPageAndSize() {
        when(bankApiClient.searchTransactions("Bearer user-token", "accessToken=user-token", null, null, 3, 25))
                .thenReturn(response());

        transactionTools.searchTransactions(null, null, 3, 25, transportContext);

        verify(bankApiClient).searchTransactions("Bearer user-token", "accessToken=user-token", null, null, 3, 25);
    }

    @Test
    void rejectsANegativePage() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> transactionTools.searchTransactions(null, null, -1, 10, transportContext))
                .withMessage("page must be zero or greater.");

        verifyNoInteractions(bankApiClient);
    }

    @Test
    void rejectsAnInvalidPageSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> transactionTools.searchTransactions(null, null, 0, 2_001, transportContext))
                .withMessage("size must be between 1 and 2000.");

        verifyNoInteractions(bankApiClient);
    }

    @Test
    void forwardsAValidatedDateRange() {
        when(bankApiClient.searchTransactions(
                "Bearer user-token", "accessToken=user-token", "2026-01-01", "2026-01-31", 1, 15
        )).thenReturn(response());

        transactionTools.searchTransactions("2026-01-01", "2026-01-31", 1, 15, transportContext);

        verify(bankApiClient).searchTransactions(
                "Bearer user-token", "accessToken=user-token", "2026-01-01", "2026-01-31", 1, 15);
    }

    private UserTransactionsResponse response() {
        return new UserTransactionsResponse(List.of(), 0, 0, 0, true);
    }
}
