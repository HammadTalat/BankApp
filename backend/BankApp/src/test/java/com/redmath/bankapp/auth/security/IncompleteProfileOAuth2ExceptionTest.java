package com.redmath.bankapp.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class IncompleteProfileOAuth2ExceptionTest {

  @Test
  @DisplayName("Should create exception with error, message, email, and name")
  void constructor_WithAllFields_CreatesException() {
    OAuth2Error error = new OAuth2Error("incomplete_profile", "Profile is incomplete", null);
    IncompleteProfileOAuth2Exception exception = new IncompleteProfileOAuth2Exception(
        error,
        "Profile incomplete for user",
        "user@example.com",
        "Test User"
    );

    assertThat(exception).isInstanceOf(OAuth2AuthenticationException.class);
    assertThat(exception.getError()).isEqualTo(error);
    assertThat(exception.getMessage()).isEqualTo("Profile incomplete for user");
    assertThat(exception.getEmail()).isEqualTo("user@example.com");
    assertThat(exception.getName()).isEqualTo("Test User");
  }

  @Test
  @DisplayName("Should return correct email via getter")
  void getEmail_ReturnsEmail() {
    OAuth2Error error = new OAuth2Error("incomplete_profile", "Profile is incomplete", null);
    IncompleteProfileOAuth2Exception exception = new IncompleteProfileOAuth2Exception(
        error,
        "Profile incomplete",
        "test@example.com",
        "Test"
    );

    assertThat(exception.getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should return correct name via getter")
  void getName_ReturnsName() {
    OAuth2Error error = new OAuth2Error("incomplete_profile", "Profile is incomplete", null);
    IncompleteProfileOAuth2Exception exception = new IncompleteProfileOAuth2Exception(
        error,
        "Profile incomplete",
        "test@example.com",
        "Test Name"
    );

    assertThat(exception.getName()).isEqualTo("Test Name");
  }
}
