package com.redmath.bankapp.auth.security;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class IncompleteProfileOAuth2Exception extends OAuth2AuthenticationException {

  private final String email;
  private final String name;

  public IncompleteProfileOAuth2Exception(
      OAuth2Error error,
      String message,
      String email,
      String name) {
    super(error, message);
    this.email = email;
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

}
