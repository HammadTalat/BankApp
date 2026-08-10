package com.redmath.bankapp.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.redmath.bankapp.auth.dto.request.SignupRequest;
import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.security.AuthCookieService;
import com.redmath.bankapp.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

  @Mock
  private AuthService authService;

  @Mock
  private AuthCookieService authCookieService;

  @Mock
  private HttpServletResponse httpServletResponse;

  @InjectMocks
  private AuthController authController;

  @Nested
  @DisplayName("signup")
  class SignupTests {

    @Test
    @DisplayName("Should delegate to AuthService and return 201 CREATED")
    void signup_ValidRequest_Returns201() {
      SignupRequest request = new SignupRequest(
          "John Doe",
          "john@example.com",
          "123 Main St",
          "password123"
      );
      SignupResponse serviceResponse = new SignupResponse(
          true,
          "Registration submitted successfully. Your account is pending administrator approval."
      );
      given(authService.signup(request)).willReturn(serviceResponse);

      ResponseEntity<SignupResponse> response = authController.signup(request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(serviceResponse);

      verify(authService).signup(request);
      verifyNoMoreInteractions(authService, authCookieService);
    }
  }

  @Nested
  @DisplayName("logout")
  class LogoutTests {

    @Test
    @DisplayName("Should clear auth cookie and return 204 NO CONTENT")
    void logout_Returns204AndClearsCookie() {
      ResponseEntity<Void> response = authController.logout(httpServletResponse);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(response.getBody()).isNull();

      verify(authCookieService).clearAccessToken(httpServletResponse);
      verifyNoMoreInteractions(authService, authCookieService);
    }
  }
}
