package com.redmath.bankapp.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.exception.DuplicateEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthExceptionHandlerTest {

  private final AuthExceptionHandler authExceptionHandler = new AuthExceptionHandler();

  @Test
  @DisplayName("Should map DuplicateEmailException to 409 CONFLICT")
  void handleDuplicateEmail_ReturnsConflictResponse() {
    DuplicateEmailException exception =
        new DuplicateEmailException("Email already exists: dup@example.com");

    ResponseEntity<SignupResponse> response =
        authExceptionHandler.handleDuplicateEmail(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().success()).isFalse();
    assertThat(response.getBody().message()).isEqualTo("Email already exists: dup@example.com");
  }
}
