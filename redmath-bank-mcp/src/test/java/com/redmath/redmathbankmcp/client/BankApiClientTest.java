package com.redmath.redmathbankmcp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParamCount;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.redmath.redmathbankmcp.dto.UserTransactionsResponse;
import com.redmath.redmathbankmcp.dto.SpendingSummaryResponse;
import com.redmath.redmathbankmcp.dto.AccountResponse;
import com.redmath.redmathbankmcp.dto.BalanceResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BankApiClientTest {

    private MockRestServiceServer server;
    private BankApiClient bankApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://bank-app");
        server = MockRestServiceServer.bindTo(builder).build();
        bankApiClient = new BankApiClient(builder.build());
    }

    @Test
    void buildsTheBackendTransactionRequestWithDatesPaginationAndAuthentication() {
        server.expect(requestTo("http://bank-app/api/v1/transaction/get-transactions?startDate=2026-01-01&endDate=2026-01-31&page=2&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("startDate", "2026-01-01"))
                .andExpect(queryParam("endDate", "2026-01-31"))
                .andExpect(queryParam("page", "2"))
                .andExpect(queryParam("size", "20"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(header(HttpHeaders.COOKIE, "accessToken=user-token"))
                .andRespond(withSuccess("{\"transactions\":[],\"currentPage\":2,\"totalPages\":1,\"totalElements\":0,\"isLast\":true}", MediaType.APPLICATION_JSON));

        UserTransactionsResponse response = bankApiClient.searchTransactions(
                "Bearer user-token", "accessToken=user-token", "2026-01-01", "2026-01-31", 2, 20);

        assertThat(response.currentPage()).isEqualTo(2);
        server.verify();
    }

    @Test
    void omitsDateParametersWhenNoDateRangeWasSupplied() {
        server.expect(requestTo("http://bank-app/api/v1/transaction/get-transactions?page=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParamCount(2))
                .andExpect(queryParam("page", "0"))
                .andExpect(queryParam("size", "10"))
                .andRespond(withSuccess("{\"transactions\":[],\"currentPage\":0,\"totalPages\":0,\"totalElements\":0,\"isLast\":true}", MediaType.APPLICATION_JSON));

        bankApiClient.searchTransactions(null, null, null, null, 0, 10);

        server.verify();
    }

    @Test
    void buildsTheSpendingSummaryRequestAndDeserializesItsResponse() {
        server.expect(requestTo("http://bank-app/api/v1/transaction/spending-summary?startDate=2026-08-01&endDate=2026-08-10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("startDate", "2026-08-01"))
                .andExpect(queryParam("endDate", "2026-08-10"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(header(HttpHeaders.COOKIE, "accessToken=user-token"))
                .andRespond(withSuccess("{\"startDate\":\"2026-08-01\",\"endDate\":\"2026-08-10\",\"totalSpent\":450.00,\"transactionCount\":3,\"largestExpense\":250.00}", MediaType.APPLICATION_JSON));

        SpendingSummaryResponse response = bankApiClient.getSpendingSummary(
                "Bearer user-token", "accessToken=user-token", "2026-08-01", "2026-08-10");

        assertThat(response.startDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 1));
        assertThat(response.totalSpent()).isEqualByComparingTo("450.00");
        assertThat(response.transactionCount()).isEqualTo(3L);
        assertThat(response.largestExpense()).isEqualByComparingTo("250.00");
        server.verify();
    }

    @Test
    void omitsSpendingSummaryDateParametersWhenNoDateRangeWasSupplied() {
        server.expect(requestTo("http://bank-app/api/v1/transaction/spending-summary"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParamCount(0))
                .andRespond(withSuccess("{\"startDate\":\"2026-08-01\",\"endDate\":\"2026-08-10\",\"totalSpent\":0,\"transactionCount\":0,\"largestExpense\":0}", MediaType.APPLICATION_JSON));

        bankApiClient.getSpendingSummary(null, null, null, null);

        server.verify();
    }

    @Test
    void buildsAuthenticatedAccountAndBalanceRequests() {
        server.expect(requestTo("http://bank-app/api/v1/account"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andRespond(withSuccess("{\"accountNumber\":\"RM-1001\",\"status\":\"ACTIVE\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://bank-app/api/v1/account/balance"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andRespond(withSuccess("{\"amount\":1250.50}", MediaType.APPLICATION_JSON));

        AccountResponse account = bankApiClient.getAccount("Bearer user-token", null);
        BalanceResponse balance = bankApiClient.getBalance("Bearer user-token", null);

        assertThat(account.accountNumber()).isEqualTo("RM-1001");
        assertThat(balance.amount()).isEqualByComparingTo("1250.50");
        server.verify();
    }
}
