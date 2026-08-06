package com.redmath.bankapp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final ApiSecurityService apiSecurityService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = getAccessToken(request);

    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication existingAuthentication =
        SecurityContextHolder.getContext().getAuthentication();

    if (existingAuthentication != null && existingAuthentication.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Jwt jwt = apiSecurityService.decodeToken(token);
      List<GrantedAuthority> authorities = extractAuthorities(jwt);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(jwt, token, authorities);

      authentication.setDetails(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException exception) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);

  }

  private String getAccessToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> AuthCookieService.ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
        .map(cookie -> cookie.getValue())
        .filter(value -> !value.isBlank())
        .findFirst()
        .orElse(null);
  }

  private List<GrantedAuthority> extractAuthorities(Jwt jwt) {

    Object scopeClaim = jwt.getClaims().get("scope");

    if (scopeClaim instanceof String scope && !scope.isBlank()) {
      return Arrays.stream(scope.split("\\s+"))
          .filter(value -> !value.isBlank())
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    }

    return List.of();

  }

}
