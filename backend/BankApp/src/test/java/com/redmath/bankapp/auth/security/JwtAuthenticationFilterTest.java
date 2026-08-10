package com.redmath.bankapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private ApiSecurityService apiSecurityService;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("when no access token cookie is present")
  class NoTokenTests {

    @Test
    @DisplayName("Should continue filter chain without setting authentication")
    void noCookies_ContinuesChain() throws Exception {
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
      verify(filterChain).doFilter(request, response);
      verifyNoInteractions(apiSecurityService);
    }

    @Test
    @DisplayName("Should ignore blank cookie values")
    void blankCookieValue_ContinuesChain() throws Exception {
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "   "));

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
      verify(filterChain).doFilter(request, response);
      verifyNoInteractions(apiSecurityService);
    }
  }

  @Nested
  @DisplayName("when user is already authenticated")
  class ExistingAuthenticationTests {

    @Test
    @DisplayName("Should continue token processing when authentication exists but is not authenticated")
    void unauthenticatedContext_ProcessesToken() throws Exception {
      SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken("anonymous", "token", List.of())
      );
      SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);

      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "valid-token"));
      Jwt jwt = Jwt.withTokenValue("valid-token")
          .header("alg", "none")
          .subject("user@example.com")
          .claim("scope", " ROLE_ACCOUNT_HOLDER")
          .build();
      given(apiSecurityService.decodeToken("valid-token")).willReturn(jwt);

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
      verify(apiSecurityService).decodeToken("valid-token");
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip token processing and continue chain")
    void existingAuthentication_SkipsTokenProcessing() throws Exception {
      SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(
              "existing-user",
              "credentials",
              List.of(new SimpleGrantedAuthority("ROLE_USER"))
          )
      );
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "existing-token"));

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
      verifyNoInteractions(apiSecurityService);
    }
  }

  @Nested
  @DisplayName("when a valid access token cookie is present")
  class ValidTokenTests {

    @Test
    @DisplayName("Should decode token and populate security context with scope authorities")
    void validToken_SetsAuthenticationWithAuthorities() throws Exception {
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "valid-token"));

      Jwt jwt = Jwt.withTokenValue("valid-token")
          .header("alg", "none")
          .subject("user@example.com")
          .claim("scope", " ROLE_ACCOUNT_HOLDER  ROLE_ADMIN")
          .build();
      given(apiSecurityService.decodeToken("valid-token")).willReturn(jwt);

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      UsernamePasswordAuthenticationToken authentication =
          (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

      assertThat(authentication).isNotNull();
      assertThat(authentication.getPrincipal()).isEqualTo(jwt);
      assertThat(authentication.getCredentials()).isEqualTo("valid-token");
      assertThat(authentication.getAuthorities())
          .extracting(Object::toString)
          .containsExactly("ROLE_ACCOUNT_HOLDER", "ROLE_ADMIN");

      verify(apiSecurityService).decodeToken("valid-token");
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should ignore non-string or blank scope claims")
    void nonStringScope_UsesEmptyAuthorities() throws Exception {
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "scopeless-token"));

      Jwt jwt = Jwt.withTokenValue("scopeless-token")
          .header("alg", "none")
          .subject("user@example.com")
          .claim("scope", 123)
          .build();
      given(apiSecurityService.decodeToken("scopeless-token")).willReturn(jwt);

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      UsernamePasswordAuthenticationToken authentication =
          (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

      assertThat(authentication.getAuthorities()).isEmpty();
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use empty authorities when scope claim is blank")
    void blankScope_UsesEmptyAuthorities() throws Exception {
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "blank-scope-token"));

      Jwt jwt = Jwt.withTokenValue("blank-scope-token")
          .header("alg", "none")
          .subject("user@example.com")
          .claim("scope", "   ")
          .build();
      given(apiSecurityService.decodeToken("blank-scope-token")).willReturn(jwt);

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      UsernamePasswordAuthenticationToken authentication =
          (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

      assertThat(authentication.getAuthorities()).isEmpty();
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  @DisplayName("when token decoding fails")
  class InvalidTokenTests {

    @Test
    @DisplayName("Should clear security context and continue filter chain")
    void invalidToken_ClearsContextAndContinuesChain() throws Exception {
      request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "invalid-token"));
      given(apiSecurityService.decodeToken("invalid-token"))
          .willThrow(mock(JwtException.class));

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
      verify(apiSecurityService).decodeToken("invalid-token");
      verify(filterChain).doFilter(request, response);
      verifyNoMoreInteractions(apiSecurityService);
    }
  }
}
