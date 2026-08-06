package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final OAuthUserResolver oauthUserResolver;

  private final ApiSecurityService apiSecurityService;

  private final AuthCookieService authCookieService;

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      Authentication authentication)
      throws IOException, ServletException {

    if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
      throw new ServletException("OAuth2 authentication principal is missing.");
    }

    AppUser appUser = oauthUserResolver.resolve(oauth2User);
    String accessToken = apiSecurityService.generateToken(appUser);
    authCookieService.addAccessToken(response, accessToken);
    String redirectUrl = frontendUrl.replaceAll("/$", "") + "/login";

    response.sendRedirect(redirectUrl);

  }

}
