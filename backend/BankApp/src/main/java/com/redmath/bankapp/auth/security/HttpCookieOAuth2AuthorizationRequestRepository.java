package com.redmath.bankapp.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";

  private static final int COOKIE_MAX_AGE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(
      HttpServletRequest request) {

    Cookie cookie = findCookie(request, COOKIE_NAME);

    if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
      return null;
    }

    return deserialize(cookie.getValue());

  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) {
      deleteCookie(response, COOKIE_NAME);
      return;
    }

    addCookie(response, COOKIE_NAME, serialize(authorizationRequest), COOKIE_MAX_AGE_SECONDS);

  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request,
      HttpServletResponse response) {

    OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
    deleteCookie(response, COOKIE_NAME);
    return authorizationRequest;

  }

  private Cookie findCookie(HttpServletRequest request, String name) {

    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;

  }

  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {

    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);

  }

  private void deleteCookie(HttpServletResponse response, String name) {

    Cookie cookie = new Cookie(name, "");
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    response.addCookie(cookie);

  }

  private String serialize(OAuth2AuthorizationRequest authorizationRequest) {

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

      objectOutputStream.writeObject(authorizationRequest);
      objectOutputStream.flush();
      return Base64.getUrlEncoder().encodeToString(outputStream.toByteArray());

    } catch (IOException exception) {
      throw new IllegalStateException("Unable to serialize OAuth2 authorization request.", exception);
    }

  }

  private OAuth2AuthorizationRequest deserialize(String value) {

    byte[] bytes = Base64.getUrlDecoder().decode(value);

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
         ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

      return (OAuth2AuthorizationRequest) objectInputStream.readObject();

    } catch (IOException | ClassNotFoundException exception) {
      throw new IllegalStateException("Unable to deserialize OAuth2 authorization request.", exception);
    }

  }

}