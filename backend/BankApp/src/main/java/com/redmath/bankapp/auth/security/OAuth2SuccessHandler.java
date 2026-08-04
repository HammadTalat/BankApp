package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.auth.dto.response.AuthResponse;
import com.redmath.bankapp.user.entity.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final OAuthUserResolver oauthUserResolver;

  private final ApiSecurityService apiSecurityService;

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException, ServletException {

    if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
      throw new ServletException("OAuth2 authentication principal is missing.");
    }

    AppUser appUser = oauthUserResolver.resolve(oauth2User);
    String accessToken = apiSecurityService.generateToken(appUser);

    AuthResponse authResponse = new AuthResponse(
        accessToken,
        "Bearer",
        ApiSecurityService.TOKEN_EXPIRATION_SECONDS,
        appUser.getEmail(),
        appUser.getName(),
        appUser.getRole().name()
    );

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(response.getOutputStream(), authResponse);

  }

}