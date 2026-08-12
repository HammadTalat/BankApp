package com.redmath.redmathbankmcp.client;

import org.springframework.web.client.RestClientResponseException;

public class BankApiException extends RuntimeException {

    public BankApiException(String message, Throwable cause) {
        super(message, cause);
    }

    static BankApiException fromResponse(RestClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();

        String message = switch (statusCode) {
            case 400 -> "BankApp rejected the transaction search request.";
            case 401 -> "Authentication is required to search transactions.";
            case 403 -> "You are not authorized to search these transactions.";
            case 404 -> "The requested transaction resource was not found.";
            default -> "BankApp could not complete the transaction search (HTTP " + statusCode + ").";
        };

        return new BankApiException(message, exception);
    }
}
