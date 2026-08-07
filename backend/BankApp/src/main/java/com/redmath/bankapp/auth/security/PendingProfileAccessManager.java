package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("pendingProfileAccessManager")
@RequiredArgsConstructor
public class PendingProfileAccessManager {

  private final AppUserRepository appUserRepository;

  public boolean hasAccess(HttpServletRequest request, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    String email = extractEmail(authentication);
    if (email == null) {
      return false;
    }

    AppUser appUser = appUserRepository.findByEmail(email).orElse(null);
    if (appUser == null) {
      return false;
    }

    if (appUser.getApprovalStatus() == ApprovalStatus.APPROVED) {
      return true;
    }

    String requestUri = request.getRequestURI();
    return "/api/v1/me".equals(requestUri)
        || requestUri.startsWith("/api/v1/me/");
  }

  private String extractEmail(Authentication authentication) {
    Object principal = authentication.getPrincipal();

    if (principal instanceof Jwt jwt) {
      return jwt.getSubject();
    }

    if (principal instanceof String email) {
      return email;
    }

    if (principal instanceof CustomUserDetails customUserDetails) {
      return customUserDetails.getUsername();
    }

    return null;
  }

}
