package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.auth.dto.response.AuthResponse;
import com.redmath.bankapp.user.entity.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationSuccessHandler
    implements AuthenticationSuccessHandler {

  private final ApiSecurityService apiSecurityService;

  private final AuthCookieService authCookieService;

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException, ServletException {

    CustomUserDetails userDetails =
        Objects.requireNonNull(
            (CustomUserDetails) authentication.getPrincipal(),
            "Authenticated principal is missing"
        );

    AppUser appUser = userDetails.getAppUser();
    if (appUser == null) {
      throw new IllegalStateException("Authenticated user details are missing appUser");
    }

    String accessToken =
        apiSecurityService.generateToken(appUser);

    authCookieService.addAccessToken(response, accessToken);

    AuthResponse authResponse = new AuthResponse(
        null,
        "Bearer",
        ApiSecurityService.TOKEN_EXPIRATION_SECONDS,
        appUser.getEmail(),
        appUser.getName(),
        appUser.getRole().name(),
        null
    );

    response.setStatus(HttpServletResponse.SC_OK);

    response.setContentType("application/json");

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(
        response.getOutputStream(),
        authResponse
    );

  }

}
