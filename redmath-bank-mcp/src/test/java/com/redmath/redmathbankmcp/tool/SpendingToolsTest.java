package com.redmath.redmathbankmcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.SpendingSummaryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpendingToolsTest {

    private final BankApiClient bankApiClient = mock(BankApiClient.class);
    private final McpTransportContext transportContext = McpTransportContext.create(Map.of(
            McpTransportConfig.AUTHORIZATION_HEADER, "Bearer user-token",
            McpTransportConfig.COOKIE_HEADER, "accessToken=user-token"
    ));

    private SpendingTools spendingTools;

    @BeforeEach
    void setUp() {
        spendingTools = new SpendingTools(bankApiClient);
    }

    @Test
    void passesOmittedDatesToBankAppSoItCanApplyItsCurrentMonthDefaults() {
        SpendingSummaryResponse expected = response();
        when(bankApiClient.getSpendingSummary("Bearer user-token", "accessToken=user-token", null, null))
                .thenReturn(expected);

        SpendingSummaryResponse actual = spendingTools.getSpendingSummary(null, null, transportContext);

        assertThat(actual).isSameAs(expected);
        verify(bankApiClient).getSpendingSummary("Bearer user-token", "accessToken=user-token", null, null);
    }

    @Test
    void forwardsProvidedDatesAndReturnsTheBackendSummary() {
        SpendingSummaryResponse expected = response();
        when(bankApiClient.getSpendingSummary(
                "Bearer user-token", "accessToken=user-token", "2026-08-01", "2026-08-10"
        )).thenReturn(expected);

        SpendingSummaryResponse actual = spendingTools.getSpendingSummary(
                "2026-08-01", "2026-08-10", transportContext);

        assertThat(actual).isSameAs(expected);
        verify(bankApiClient).getSpendingSummary(
                "Bearer user-token", "accessToken=user-token", "2026-08-01", "2026-08-10");
    }

    @Test
    void rejectsInvalidToolDateInputBeforeCallingBankApp() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> spendingTools.getSpendingSummary("2026-08-11", "2026-08-10", transportContext))
                .withMessage("startDate must be on or before endDate.");

        verifyNoInteractions(bankApiClient);
    }

    @Test
    void rejectsMalformedToolDateInputBeforeCallingBankApp() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> spendingTools.getSpendingSummary("08-01-2026", null, transportContext))
                .withMessage("startDate must use YYYY-MM-DD format.");

        verifyNoInteractions(bankApiClient);
    }

    private SpendingSummaryResponse response() {
        return new SpendingSummaryResponse(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                new BigDecimal("450.00"),
                3L,
                new BigDecimal("250.00")
        );
    }
}
