package com.redmath.bankapp.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  @Override
  public void onAuthenticationFailure(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull AuthenticationException exception)
      throws IOException {

    String message;
    String errorCode = null;
    String redirectPath = null;
    String email = null;
    String name = null;

    switch (exception) {
      case DisabledException disabledException ->
          message = "Your account is awaiting administrator approval.";
      case IncompleteProfileOAuth2Exception incompleteException -> {

        message = incompleteException.getMessage();
        errorCode = incompleteException.getError().getErrorCode();
        redirectPath = "/complete-profile";
        email = incompleteException.getEmail();
        name = incompleteException.getName();
      }
      case OAuth2AuthenticationException oauth2Exception -> {

        String description = oauth2Exception.getError().getDescription();

        if (description != null && !description.isBlank()) {
          message = description;
        } else if (oauth2Exception.getError().getErrorCode() != null) {
          message = switch (oauth2Exception.getError().getErrorCode()) {
            case "access_denied" -> "Google sign-in was cancelled or denied.";
            case "invalid_request" -> "Google sign-in request was invalid.";
            case "server_error" -> "Google sign-in service is temporarily unavailable.";
            case "account_not_approved" -> "Your account is awaiting administrator approval.";
            default -> "Google sign-in failed.";
          };
          errorCode = oauth2Exception.getError().getErrorCode();
        } else {
          message = "Google sign-in failed.";
        }
      }
      case LockedException lockedException -> message = "Your account has been locked.";
      case CredentialsExpiredException credentialsExpiredException ->
          message = "Your credentials have expired.";
      case BadCredentialsException badCredentialsException ->
          message = "Invalid email or password.";
      default -> message = "Authentication failed.";
    }

    if (isOAuthRequest(request)) {
      response.sendRedirect(frontendUrl.replaceAll("/$", "")
          + "/login#oauthError="
          + URLEncoder.encode(message, StandardCharsets.UTF_8));
      return;
    }

    Map<String, Object> body = new LinkedHashMap<>();

    body.put("timestamp", Instant.now());

    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

    body.put("error", "Unauthorized");

    body.put("message", message);

    body.put("path", request.getRequestURI());

    if (errorCode != null) {
      body.put("errorCode", errorCode);
    }

    if (redirectPath != null) {
      body.put("redirectPath", redirectPath);
    }

    if (email != null) {
      body.put("email", email);
    }

    if (name != null) {
      body.put("name", name);
    }

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    response.setContentType("application/json");

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(
        response.getOutputStream(),
        body
    );

  }

  private boolean isOAuthRequest(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    return requestUri.startsWith("/oauth2/")
        || requestUri.startsWith("/login/oauth2/");
  }

}
