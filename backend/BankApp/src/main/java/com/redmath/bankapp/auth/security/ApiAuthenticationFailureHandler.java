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