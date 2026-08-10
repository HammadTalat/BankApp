package com.redmath.bankapp.auth.controller;

import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<SignupResponse> handleDuplicateEmail(DuplicateEmailException exception) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(new SignupResponse(false, exception.getMessage()));
  }
}
