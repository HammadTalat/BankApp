package com.redmath.redmathbankmcp.client;

import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;
import com.redmath.redmathbankmcp.dto.SpendingSummaryResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BankApiClient {

    private final RestClient restClient;

    public BankApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UserTransactionsResponse searchTransactions(
            String authorization,
            String cookie,
            String startDate,
            String endDate,
            int page,
            int size
    ) {

        try {
            return restClient
                    .get()

                .uri(uriBuilder -> {

                    uriBuilder.path(
                            "/api/v1/transaction/get-transactions"
                    );

                    if (StringUtils.hasText(startDate)) {
                        uriBuilder.queryParam(
                                "startDate",
                                startDate
                        );
                    }

                    if (StringUtils.hasText(endDate)) {
                        uriBuilder.queryParam(
                                "endDate",
                                endDate
                        );
                    }

                    uriBuilder.queryParam("page", page);
                    uriBuilder.queryParam("size", size);

                    return uriBuilder.build();
                })

                .headers(headers -> {

                    if (StringUtils.hasText(authorization)) {
                        headers.set(
                                HttpHeaders.AUTHORIZATION,
                                authorization
                        );
                    }

                    if (StringUtils.hasText(cookie)) {
                        headers.set(
                                HttpHeaders.COOKIE,
                                cookie
                        );
                    }
                })

                .retrieve()

                    .body(UserTransactionsResponse.class);
        }
        catch (RestClientResponseException exception) {
            throw BankApiException.fromResponse(exception);
        }
        catch (ResourceAccessException exception) {
            throw new BankApiException("RedMath Bank is currently unavailable. Please try again later.", exception);
        }
        catch (RestClientException exception) {
            throw new BankApiException("The transaction request could not be completed.", exception);
        }
    }

    public SpendingSummaryResponse getSpendingSummary(
            String authorization,
            String cookie,
            String startDate,
            String endDate
    ) {
        try {
            return restClient
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/transaction/spending-summary");

                        if (StringUtils.hasText(startDate)) {
                            uriBuilder.queryParam("startDate", startDate);
                        }

                        if (StringUtils.hasText(endDate)) {
                            uriBuilder.queryParam("endDate", endDate);
                        }

                        return uriBuilder.build();
                    })
                    .headers(headers -> {
                        if (StringUtils.hasText(authorization)) {
                            headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        }

                        if (StringUtils.hasText(cookie)) {
                            headers.set(HttpHeaders.COOKIE, cookie);
                        }
                    })
                    .retrieve()
                    .body(SpendingSummaryResponse.class);
        }
        catch (RestClientResponseException exception) {
            throw BankApiException.fromResponse(exception);
        }
        catch (ResourceAccessException exception) {
            throw new BankApiException("RedMath Bank is currently unavailable. Please try again later.", exception);
        }
        catch (RestClientException exception) {
            throw new BankApiException("The spending summary request could not be completed.", exception);
        }
    }
}
