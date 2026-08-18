package com.redmath.bankapp.admin.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class AdminExceptionHandlerAdditionalTest {

    private final AdminExceptionHandler handler = new AdminExceptionHandler();

    @Test
    void returnsDetailedFieldErrorsForConstraintViolations() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("request.page");
        when(violation.getMessage()).thenReturn("must be greater than or equal to zero");

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation)), request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody().getProperties())
                .containsEntry("errors", Map.of(
                        "request.page", "must be greater than or equal to zero"
                ));
    }

    @Test
    void returnsPublicProblemDetailsForFrameworkAndDataErrors() throws Exception {
        assertProblem(
                handler.handleMethodValidation(mock(HandlerMethodValidationException.class), request()),
                HttpStatus.BAD_REQUEST,
                "ADMIN_VALIDATION_FAILED"
        );
        assertProblem(
                handler.handleDataConflict(new DataIntegrityViolationException("duplicate"), request()),
                HttpStatus.CONFLICT,
                "ADMIN_DATA_CONFLICT"
        );
        assertProblem(
                handler.handleUnsupportedMediaType(
                        new HttpMediaTypeNotSupportedException(
                                MediaType.APPLICATION_XML,
                                List.of(MediaType.APPLICATION_JSON)
                        ),
                        request()
                ),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "ADMIN_UNSUPPORTED_MEDIA_TYPE"
        );
        assertProblem(
                handler.handleUnacceptableMediaType(
                        new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)),
                        request()
                ),
                HttpStatus.NOT_ACCEPTABLE,
                "ADMIN_MEDIA_TYPE_NOT_ACCEPTABLE"
        );
        assertProblem(
                handler.handleUnsupportedMethod(
                        new HttpRequestMethodNotSupportedException("PUT"),
                        request()
                ),
                HttpStatus.METHOD_NOT_ALLOWED,
                "ADMIN_METHOD_NOT_ALLOWED"
        );
    }

    @Test
    void formatsNonEnumTypeMismatchAndFallsBackForBlankFieldValidationMessages() {
        MethodArgumentTypeMismatchException mismatch = mock(MethodArgumentTypeMismatchException.class);
        when(mismatch.getValue()).thenReturn("not-a-number");
        when(mismatch.getName()).thenReturn("size");
        doReturn(Integer.class).when(mismatch).getRequiredType();

        ResponseEntity<ProblemDetail> mismatchResponse = handler.handleTypeMismatch(mismatch, request());
        assertThat(mismatchResponse.getBody().getDetail())
                .isEqualTo("Invalid value 'not-a-number' for parameter 'size'. Expected type: Integer");

        MethodArgumentNotValidException validation = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(validation.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "name", " ")
        ));

        ResponseEntity<ProblemDetail> validationResponse = handler.handleInvalidMethodArgument(
                validation,
                request()
        );
        assertThat(validationResponse.getBody().getProperties())
                .containsEntry("errors", Map.of("name", "Invalid value"));
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin/test");
        return request;
    }

    private void assertProblem(
            ResponseEntity<ProblemDetail> response,
            HttpStatus status,
            String code
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody().getProperties()).containsEntry("code", code);
    }
}
