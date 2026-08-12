package com.redmath.redmathbankmcp.tool;

import java.time.LocalDate;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.AccountResponse;
import com.redmath.redmathbankmcp.dto.AccountSummaryResponse;
import com.redmath.redmathbankmcp.dto.BalanceResponse;
import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class AccountTools {

    private static final int DEFAULT_RECENT_TRANSACTION_LIMIT = 10;
    private static final int MAX_RECENT_TRANSACTION_LIMIT = 20;

    private final BankApiClient bankApiClient;

    public AccountTools(BankApiClient bankApiClient) {
        this.bankApiClient = bankApiClient;
    }

    @McpTool(
            name = "get_account_summary",
            description = "Returns the authenticated RedMath Bank account holder's account number, account status, and current balance.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false),
            generateOutputSchema = true
    )
    public AccountSummaryResponse getAccountSummary(McpTransportContext transportContext) {
        String authorization = headerValue(transportContext, McpTransportConfig.AUTHORIZATION_HEADER);
        String cookie = headerValue(transportContext, McpTransportConfig.COOKIE_HEADER);
        AccountResponse account = bankApiClient.getAccount(authorization, cookie);
        BalanceResponse balance = bankApiClient.getBalance(authorization, cookie);

        return new AccountSummaryResponse(account.accountNumber(), account.status(), balance.amount());
    }

    @McpTool(
            name = "get_recent_transactions",
            description = "Returns the authenticated RedMath Bank account holder's transactions from the last 30 days.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false),
            generateOutputSchema = true
    )
    public UserTransactionsResponse getRecentTransactions(
            @McpToolParam(required = false, description = "Optional number of recent transactions to return, from 1 to 20. Defaults to 10.")
            Integer limit,
            McpTransportContext transportContext
    ) {
        int effectiveLimit = limit == null ? DEFAULT_RECENT_TRANSACTION_LIMIT : limit;
        if (effectiveLimit < 1 || effectiveLimit > MAX_RECENT_TRANSACTION_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and 20.");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return bankApiClient.searchTransactions(
                headerValue(transportContext, McpTransportConfig.AUTHORIZATION_HEADER),
                headerValue(transportContext, McpTransportConfig.COOKIE_HEADER),
                startDate.toString(),
                endDate.toString(),
                0,
                effectiveLimit
        );
    }

    private String headerValue(McpTransportContext transportContext, String headerName) {
        if (transportContext == null) {
            return null;
        }

        Object value = transportContext.get(headerName);
        return value instanceof String headerValue ? headerValue : null;
    }
}
