package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUserResolver {

  private final AppUserRepository appUserRepository;

  public AppUser resolve(OAuth2User oauth2User) {

    String email = extractEmail(oauth2User.getAttributes());

    AppUser appUser = appUserRepository.findByEmail(email)
        .orElseThrow(() -> new OAuth2AuthenticationException(
            new OAuth2Error("user_not_found"),
            "No local account exists for OAuth2 email: " + email
        ));

    if (appUser.getApprovalStatus() != ApprovalStatus.APPROVED) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("account_not_approved"),
          "Your account is awaiting administrator approval."
      );
    }

    return appUser;

  }

  private String extractEmail(Map<String, Object> attributes) {

    Object emailAttribute = attributes.get("email");

    if (emailAttribute instanceof String email && !email.isBlank()) {
      return email;
    }

    throw new OAuth2AuthenticationException(
        new OAuth2Error("missing_email"),
        "OAuth2 provider did not return an email address."
    );

  }

}