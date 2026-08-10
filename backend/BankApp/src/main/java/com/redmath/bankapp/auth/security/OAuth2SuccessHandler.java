package com.redmath.bankapp.auth.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import com.redmath.bankapp.user.entity.AppUser;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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

  @PostConstruct
  void validateFrontendUrl() {
    if (frontendUrl == null || frontendUrl.isBlank()
        || (!frontendUrl.startsWith("http://") && !frontendUrl.startsWith("https://"))) {
      throw new IllegalStateException(
          "Invalid app.frontend-url: must be a non-empty http/https URL");
    }
  }

  @SuppressFBWarnings(
      value = "UNVALIDATED_REDIRECT",
      justification = "frontendUrl is a server-configured property, not user input")
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
    authCookieService.addAccessToken(response, accessToken);
    String redirectUrl = frontendUrl.replaceAll("/$", "") + "/login";

    response.sendRedirect(redirectUrl);

  }

}
