package com.redmath.bankapp.admin.exception;

import com.redmath.bankapp.admin.controller.AdminUserController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackageClasses = AdminUserController.class)
public class AdminExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                "ADMIN_RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidUpdateRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidUpdateRequest(
            InvalidUpdateRequestException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid update request",
                "ADMIN_INVALID_UPDATE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "Duplicate email",
                "ADMIN_DUPLICATE_EMAIL",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidUserState(
            InvalidUserStateException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "Invalid user state",
                "ADMIN_INVALID_USER_STATE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidAccountStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAccountState(
            InvalidAccountStateException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "Invalid account state",
                "ADMIN_INVALID_ACCOUNT_STATE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleInvalidMethodArgument(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        defaultMessage(error.getDefaultMessage())
                )
        );

        ProblemDetail detail = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "ADMIN_VALIDATION_FAILED",
                "One or more request fields are invalid",
                request
        );
        detail.setProperty("errors", errors);

        return response(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        ProblemDetail detail = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "ADMIN_VALIDATION_FAILED",
                "One or more request values are invalid",
                request
        );
        detail.setProperty("errors", errors);

        return response(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "ADMIN_VALIDATION_FAILED",
                "One or more request parameters are invalid",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter",
                "ADMIN_INVALID_PARAMETER",
                typeMismatchMessage(exception),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String message = "Required parameter '%s' is missing"
                .formatted(exception.getParameterName());

        return problem(
                HttpStatus.BAD_REQUEST,
                "Missing request parameter",
                "ADMIN_MISSING_PARAMETER",
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                "ADMIN_MALFORMED_REQUEST",
                "Request body is missing, malformed, or contains an invalid value",
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                "ADMIN_INVALID_PARAMETER",
                messageOrDefault(exception, "The request contains an invalid value"),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "Data conflict",
                "ADMIN_DATA_CONFLICT",
                "The request conflicts with existing application data",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                "ADMIN_UNSUPPORTED_MEDIA_TYPE",
                "The request Content-Type is not supported",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleUnacceptableMediaType(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_ACCEPTABLE,
                "Response media type not acceptable",
                "ADMIN_MEDIA_TYPE_NOT_ACCEPTABLE",
                "The requested response media type is not supported",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMethod(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        String message = "HTTP method '%s' is not supported for this endpoint"
                .formatted(exception.getMethod());

        return problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                "ADMIN_METHOD_NOT_ALLOWED",
                message,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Unexpected error while processing admin request {}",
                request.getRequestURI(),
                exception
        );

        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "ADMIN_INTERNAL_ERROR",
                "The server could not complete the admin request",
                request
        );
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ProblemDetail detail = createProblem(
                status,
                title,
                code,
                message,
                request
        );

        return response(status, detail);
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                status,
                message
        );
        detail.setTitle(title);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);
        detail.setProperty("timestamp", Instant.now().toString());

        return detail;
    }

    private ResponseEntity<ProblemDetail> response(
            HttpStatus status,
            ProblemDetail detail
    ) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }

    private String typeMismatchMessage(
            MethodArgumentTypeMismatchException exception
    ) {
        String message = "Invalid value '%s' for parameter '%s'"
                .formatted(exception.getValue(), exception.getName());
        Class<?> requiredType = exception.getRequiredType();

        if (requiredType == null) {
            return message;
        }

        if (requiredType.isEnum()) {
            String allowedValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return message + ". Allowed values: " + allowedValues;
        }

        return message + ". Expected type: " + requiredType.getSimpleName();
    }

    private String messageOrDefault(
            Exception exception,
            String defaultMessage
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return defaultMessage;
        }

        return message;
    }

    private String defaultMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Invalid value";
        }

        return message;
    }
}
