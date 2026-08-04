package com.redmath.bankapp.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationFailureHandler
    implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception)
      throws IOException, ServletException {

    String message;

    if (exception instanceof DisabledException) {

      message = "Your account is awaiting administrator approval.";

    } else if (exception instanceof OAuth2AuthenticationException oauth2Exception) {

      String description = oauth2Exception.getError().getDescription();

      if (description != null && !description.isBlank()) {
        message = description;
      } else if (oauth2Exception.getError().getErrorCode() != null) {
        message = switch (oauth2Exception.getError().getErrorCode()) {
          case "access_denied" -> "Google sign-in was cancelled or denied.";
          case "invalid_request" -> "Google sign-in request was invalid.";
          case "server_error" -> "Google sign-in service is temporarily unavailable.";
          default -> "Google sign-in failed.";
        };
      } else {
        message = "Google sign-in failed.";
      }

    } else if (exception instanceof LockedException) {

      message = "Your account has been locked.";

    } else if (exception instanceof CredentialsExpiredException) {

      message = "Your credentials have expired.";

    } else if (exception instanceof BadCredentialsException) {

      message = "Invalid email or password.";

    } else {

      message = "Authentication failed.";

    }

    Map<String, Object> body = new LinkedHashMap<>();

    body.put("timestamp", Instant.now());

    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

    body.put("error", "Unauthorized");

    body.put("message", message);

    body.put("path", request.getRequestURI());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    response.setContentType("application/json");

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(
        response.getOutputStream(),
        body
    );

  }

}