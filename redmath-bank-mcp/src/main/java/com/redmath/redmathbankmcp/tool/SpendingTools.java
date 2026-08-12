package com.redmath.redmathbankmcp.tool;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import io.modelcontextprotocol.common.McpTransportContext;

import com.redmath.redmathbankmcp.client.BankApiClient;
import com.redmath.redmathbankmcp.config.McpTransportConfig;
import com.redmath.redmathbankmcp.dto.SpendingSummaryResponse;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SpendingTools {

    private final BankApiClient bankApiClient;

    public SpendingTools(BankApiClient bankApiClient) {
        this.bankApiClient = bankApiClient;
    }

    @McpTool(
            name = "get_spending_summary",
            description = "Returns a spending summary for the authenticated RedMath Bank account holder over an optional date range. Spending includes debit transactions only and returns total spent, number of expenses, and largest expense. If dates are omitted, BankApp uses the current calendar month through today.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false),
            generateOutputSchema = true
    )
    public SpendingSummaryResponse getSpendingSummary(
            @McpToolParam(required = false, description = "Optional start date in YYYY-MM-DD format.")
            String startDate,
            @McpToolParam(required = false, description = "Optional end date in YYYY-MM-DD format.")
            String endDate,
            McpTransportContext transportContext
    ) {
        LocalDate parsedStartDate = parseDate(startDate, "startDate");
        LocalDate parsedEndDate = parseDate(endDate, "endDate");

        if (parsedStartDate != null && parsedEndDate != null && parsedStartDate.isAfter(parsedEndDate)) {
            throw new IllegalArgumentException("startDate must be on or before endDate.");
        }

        return bankApiClient.getSpendingSummary(
                headerValue(transportContext, McpTransportConfig.AUTHORIZATION_HEADER),
                headerValue(transportContext, McpTransportConfig.COOKIE_HEADER),
                parsedStartDate == null ? null : parsedStartDate.toString(),
                parsedEndDate == null ? null : parsedEndDate.toString()
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

    private String headerValue(McpTransportContext transportContext, String headerName) {
        if (transportContext == null) {
            return null;
        }

        Object value = transportContext.get(headerName);
        return value instanceof String headerValue ? headerValue : null;
    }
}
