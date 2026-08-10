package com.redmath.bankapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import tools.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ApiAuthenticationFailureHandlerTest {

  private ApiAuthenticationFailureHandler handler;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    handler = new ApiAuthenticationFailureHandler(objectMapper);
    ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:5173/");
  }

  private JsonNode invokeJsonFailure(
      String requestUri,
      AuthenticationException exception) throws Exception {

    MockHttpServletRequest request = new MockHttpServletRequest("POST", requestUri);
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(request, response, exception);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(response.getContentType()).contains("application/json");

    return objectMapper.readTree(response.getContentAsString());
  }

  @Nested
  @DisplayName("JSON error responses")
  class JsonResponses {

    @Test
    @DisplayName("Should return approval message for DisabledException")
    void disabledException_ReturnsApprovalMessage() throws Exception {
      JsonNode body = invokeJsonFailure(
          "/api/v1/auth/login",
          new DisabledException("disabled")
      );

      assertThat(body.get("message").asText())
          .isEqualTo("Your account is awaiting administrator approval.");
      assertThat(body.get("path").asText()).isEqualTo("/api/v1/auth/login");
      assertThat(body.has("errorCode")).isFalse();
    }

    @Test
    @DisplayName("Should return profile completion details for IncompleteProfileOAuth2Exception")
    void incompleteProfileException_ReturnsProfileDetails() throws Exception {
      IncompleteProfileOAuth2Exception exception = new IncompleteProfileOAuth2Exception(
          new OAuth2Error("incomplete_profile", "Profile incomplete", null),
          "Please complete your profile.",
          "user@example.com",
          "Test User"
      );

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText()).isEqualTo("Please complete your profile.");
      assertThat(body.get("errorCode").asText()).isEqualTo("incomplete_profile");
      assertThat(body.get("redirectPath").asText()).isEqualTo("/complete-profile");
      assertThat(body.get("email").asText()).isEqualTo("user@example.com");
      assertThat(body.get("name").asText()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should fall through blank OAuth2 descriptions to error code mapping")
    void oauth2Exception_WithBlankDescription_UsesErrorCodeMessage() throws Exception {
      OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
          new OAuth2Error("access_denied", "   ", null)
      );

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText())
          .isEqualTo("Google sign-in was cancelled or denied.");
      assertThat(body.get("errorCode").asText()).isEqualTo("access_denied");
    }

    @Test
    @DisplayName("Should use OAuth2 error description when present")
    void oauth2Exception_WithDescription_UsesDescription() throws Exception {
      OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
          new OAuth2Error("invalid_grant", "Token expired.", null)
      );

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText()).isEqualTo("Token expired.");
      assertThat(body.has("errorCode")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "access_denied, Google sign-in was cancelled or denied.",
        "invalid_request, Google sign-in request was invalid.",
        "server_error, Google sign-in service is temporarily unavailable.",
        "account_not_approved, Your account is awaiting administrator approval.",
        "unknown_error, Google sign-in failed."
    })
    @DisplayName("Should map OAuth2 error codes to user-friendly messages")
    void oauth2Exception_WithErrorCode_MapsMessage(String errorCode, String expectedMessage)
        throws Exception {

      OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
          new OAuth2Error(errorCode, null, null)
      );

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText()).isEqualTo(expectedMessage);
      assertThat(body.get("errorCode").asText()).isEqualTo(errorCode);
    }

    @Test
    @DisplayName("Should use default OAuth2 message when error has no code or description")
    void oauth2Exception_WithoutCodeOrDescription_UsesDefaultMessage() throws Exception {
      OAuth2AuthenticationException exception = mock(OAuth2AuthenticationException.class);
      OAuth2Error error = mock(OAuth2Error.class);
      given(exception.getError()).willReturn(error);
      given(error.getDescription()).willReturn(null);
      given(error.getErrorCode()).willReturn(null);

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText()).isEqualTo("Google sign-in failed.");
      assertThat(body.has("errorCode")).isFalse();
    }

    @Test
    @DisplayName("Should return locked account message for LockedException")
    void lockedException_ReturnsLockedMessage() throws Exception {
      JsonNode body = invokeJsonFailure(
          "/api/v1/auth/login",
          new LockedException("locked")
      );

      assertThat(body.get("message").asText()).isEqualTo("Your account has been locked.");
    }

    @Test
    @DisplayName("Should return expired credentials message for CredentialsExpiredException")
    void credentialsExpiredException_ReturnsExpiredMessage() throws Exception {
      JsonNode body = invokeJsonFailure(
          "/api/v1/auth/login",
          new CredentialsExpiredException("expired")
      );

      assertThat(body.get("message").asText()).isEqualTo("Your credentials have expired.");
    }

    @Test
    @DisplayName("Should return invalid credentials message for BadCredentialsException")
    void badCredentialsException_ReturnsInvalidCredentialsMessage() throws Exception {
      JsonNode body = invokeJsonFailure(
          "/api/v1/auth/login",
          new BadCredentialsException("bad")
      );

      assertThat(body.get("message").asText()).isEqualTo("Invalid email or password.");
    }

    @Test
    @DisplayName("Should return generic message for unknown authentication failures")
    void genericException_ReturnsGenericMessage() throws Exception {
      AuthenticationException exception = mock(AuthenticationException.class);

      JsonNode body = invokeJsonFailure("/api/v1/auth/login", exception);

      assertThat(body.get("message").asText()).isEqualTo("Authentication failed.");
    }
  }

  @Nested
  @DisplayName("OAuth redirect responses")
  class OAuthRedirects {

    @ParameterizedTest
    @CsvSource({
        "/oauth2/authorization/google",
        "/login/oauth2/code/google"
    })
    @DisplayName("Should redirect OAuth requests with encoded error fragment")
    void oauthRequest_RedirectsWithEncodedError(String requestUri) throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
      MockHttpServletResponse response = new MockHttpServletResponse();

      handler.onAuthenticationFailure(
          request,
          response,
          new BadCredentialsException("bad")
      );

      assertThat(response.getRedirectedUrl())
          .isEqualTo("http://localhost:5173/login#oauthError=Invalid+email+or+password.");
    }
  }
}
