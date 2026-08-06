package com.redmath.bankapp.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

  public static final String ACCESS_TOKEN_COOKIE = "bankapp_access_token";

  @Value("${app.auth.cookie-secure:false}")
  private boolean secureCookie;

  public void addAccessToken(HttpServletResponse response, String accessToken) {
    response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie
        .from(ACCESS_TOKEN_COOKIE, accessToken)
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite("Strict")
        .path("/")
        .maxAge(ApiSecurityService.TOKEN_EXPIRATION_SECONDS)
        .build()
        .toString());
  }

  public void clearAccessToken(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie
        .from(ACCESS_TOKEN_COOKIE, "")
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build()
        .toString());
  }

}
