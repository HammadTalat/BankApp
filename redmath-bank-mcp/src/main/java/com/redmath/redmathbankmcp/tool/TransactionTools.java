package com.redmath.redmathbankmcp.tool;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TransactionTools {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 2_000;

    private final BankApiClient bankApiClient;

    public TransactionTools(BankApiClient bankApiClient) {
        this.bankApiClient = bankApiClient;
    }

    @McpTool(
            name = "search_transactions",
            description = "Search the authenticated RedMath Bank user's transactions using an optional date range and pagination.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false),
            generateOutputSchema = true
    )
    public UserTransactionsResponse searchTransactions(
            @McpToolParam(required = false, description = "Optional first transaction date in YYYY-MM-DD format.")
            String startDate,
            @McpToolParam(required = false, description = "Optional last transaction date in YYYY-MM-DD format.")
            String endDate,
            @McpToolParam(required = false, description = "Optional zero-based page number. Defaults to 0.")
            Integer page,
            @McpToolParam(required = false, description = "Optional number of records to return, from 1 to 2000. Defaults to 10.")
            Integer size,
            McpTransportContext transportContext
    ) {
        LocalDate parsedStartDate = parseDate(startDate, "startDate");
        LocalDate parsedEndDate = parseDate(endDate, "endDate");
        validateDateRange(parsedStartDate, parsedEndDate);

        int requestedPage = page == null ? DEFAULT_PAGE : page;
        int requestedSize = size == null ? DEFAULT_SIZE : size;
        validatePage(requestedPage);
        validateSize(requestedSize);

        return bankApiClient.searchTransactions(
                headerValue(transportContext, McpTransportConfig.AUTHORIZATION_HEADER),
                headerValue(transportContext, McpTransportConfig.COOKIE_HEADER),
                parsedStartDate == null ? null : parsedStartDate.toString(),
                parsedEndDate == null ? null : parsedEndDate.toString(),
                requestedPage,
                requestedSize
        );
    }

    private LocalDate parseDate(String value, String parameterName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(parameterName + " must use YYYY-MM-DD format.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be on or before endDate.");
        }
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater.");
        }
    }

    private void validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
    }

    private String headerValue(McpTransportContext transportContext, String headerName) {
        if (transportContext == null) {
            return null;
        }

        Object value = transportContext.get(headerName);
        return value instanceof String headerValue ? headerValue : null;
    }
}
