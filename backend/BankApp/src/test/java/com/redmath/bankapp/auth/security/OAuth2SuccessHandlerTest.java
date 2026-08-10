package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import jakarta.servlet.ServletException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OAuth2SuccessHandlerTest {

  private OAuthUserResolver oauthUserResolver;
  private ApiSecurityService apiSecurityService;
  private AuthCookieService authCookieService;
  private OAuth2SuccessHandler oAuth2SuccessHandler;

  @BeforeEach
  void setUp() {
    oauthUserResolver = mock(OAuthUserResolver.class);
    apiSecurityService = mock(ApiSecurityService.class);
    authCookieService = mock(AuthCookieService.class);
    oAuth2SuccessHandler = new OAuth2SuccessHandler(oauthUserResolver, apiSecurityService, authCookieService);
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "frontendUrl", "http://localhost:5173");
  }

  @Test
  @DisplayName("Should resolve user, set cookie, and redirect on successful OAuth2 login")
  void onAuthenticationSuccess_ValidOAuth2User_ReturnsRedirect() throws Exception {
    AppUser appUser = AppUser.builder()
        .id(1L)
        .name("OAuth User")
        .email("oauth@example.com")
        .address("OAuth Address")
        .role(Role.ACCOUNT_HOLDER)
        .approvalStatus(ApprovalStatus.PENDING)
        .build();

    given(oauthUserResolver.resolve(any(OAuth2User.class))).willReturn(appUser);
    given(apiSecurityService.generateToken(appUser)).willReturn("mock-jwt-token");

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "oauth@example.com");
    attributes.put("name", "OAuth User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    Authentication authentication = mock(Authentication.class);
    given(authentication.getPrincipal()).willReturn(oauth2User);

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

    verify(oauthUserResolver).resolve(oauth2User);
    verify(apiSecurityService).generateToken(appUser);
    verify(authCookieService).addAccessToken(response, "mock-jwt-token");
    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login");
  }

  @Test
  @DisplayName("Should strip trailing slash from frontend URL before redirecting")
  void onAuthenticationSuccess_TrailingSlashFrontendUrl_RedirectsWithoutDuplicateSlash()
      throws Exception {
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "frontendUrl", "http://localhost:5173/");

    AppUser appUser = AppUser.builder()
        .email("slash@example.com")
        .name("Slash User")
        .build();
    given(oauthUserResolver.resolve(any(OAuth2User.class))).willReturn(appUser);
    given(apiSecurityService.generateToken(appUser)).willReturn("token");

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "slash@example.com");
    attributes.put("name", "Slash User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    Authentication authentication = mock(Authentication.class);
    given(authentication.getPrincipal()).willReturn(oauth2User);

    MockHttpServletResponse response = new MockHttpServletResponse();
    oAuth2SuccessHandler.onAuthenticationSuccess(
        new MockHttpServletRequest(),
        response,
        authentication
    );

    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login");
  }

  @Test
  @DisplayName("Should throw ServletException when principal is not OAuth2User")
  void onAuthenticationSuccess_InvalidPrincipal_ThrowsServletException() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    Authentication authentication = mock(Authentication.class);

    given(authentication.getPrincipal()).willReturn("not-an-oauth2-user");

    assertThatThrownBy(() ->
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
    ).isInstanceOf(ServletException.class)
      .hasMessage("OAuth2 authentication principal is missing.");
  }
}
