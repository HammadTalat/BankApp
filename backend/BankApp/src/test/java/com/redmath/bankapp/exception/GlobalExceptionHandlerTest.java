package com.redmath.bankapp.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.transaction.exception.InsufficientBalanceException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestClientException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIllegalStateDownstreamAndInsufficientBalanceFailures() {
        assertError(
                handler.handleIllegalState(new IllegalStateException("not allowed")),
                HttpStatus.BAD_REQUEST, "Invalid Request", "not allowed"
        );
        assertError(
                handler.handleRestClientException(new RestClientException("offline")),
                HttpStatus.SERVICE_UNAVAILABLE, "Service Error",
                "Risk evaluation service is temporarily unavailable."
        );
        assertError(
                handler.handleInsufficientBalance(new InsufficientBalanceException("balance too low")),
                HttpStatus.BAD_REQUEST, "Insufficient Funds", "balance too low"
        );
    }

    @Test
    void returnsEveryFieldValidationError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "amount", "must be positive"),
                new FieldError("request", "description", "must not be blank")
        ));

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation Failed");
        assertThat(response.getBody().get("details")).isEqualTo(Map.of(
                "amount", "must be positive", "description", "must not be blank"
        ));
    }

    private void assertError(
            ResponseEntity<Map<String, Object>> response,
            HttpStatus status,
            String error,
            String message
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsEntry("error", error).containsEntry("message", message);
    }
}
