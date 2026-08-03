package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.auth.dto.response.AuthResponse;
import com.redmath.bankapp.user.entity.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final OAuthUserResolver oauthUserResolver;

  private final ApiSecurityService apiSecurityService;

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

  String redirectUri = "/oauth2/success.html?accessToken=" + URLEncoder.encode(
    authResponse.accessToken(),
    StandardCharsets.UTF_8
  ) + "&tokenType=" + URLEncoder.encode(
    authResponse.tokenType(),
    StandardCharsets.UTF_8
  ) + "&expiresIn=" + authResponse.expiresIn()
    + "&email=" + URLEncoder.encode(authResponse.email(), StandardCharsets.UTF_8)
    + "&name=" + URLEncoder.encode(authResponse.name(), StandardCharsets.UTF_8)
    + "&role=" + URLEncoder.encode(authResponse.role(), StandardCharsets.UTF_8);

  response.sendRedirect(redirectUri);

  }

}