package com.redmath.bankapp.auth.controller;

import com.redmath.bankapp.auth.dto.request.SignupRequest;
import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.exception.DuplicateEmailException;
import com.redmath.bankapp.auth.security.AuthCookieService;
import com.redmath.bankapp.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AuthCookieService authCookieService;

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(
      @Valid @RequestBody SignupRequest request) {

    try {
      SignupResponse response = authService.signup(request);
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(response);
    } catch (DuplicateEmailException ex) {
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(new SignupResponse(false, ex.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    authCookieService.clearAccessToken(response);
    return ResponseEntity.noContent().build();
  }

}
