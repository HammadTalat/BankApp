package com.redmath.bankapp.auth.controller;

import com.redmath.bankapp.auth.dto.request.SignupRequest;
import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(
      @Valid @RequestBody SignupRequest request) {

    SignupResponse response = authService.signup(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

}