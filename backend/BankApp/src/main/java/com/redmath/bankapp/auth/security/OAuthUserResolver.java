package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
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
        .orElseGet(() -> createGoogleUser(oauth2User));

    if (appUser.getApprovalStatus() == ApprovalStatus.PENDING) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("account_not_approved"),
          "Your account is awaiting administrator approval."
      );
    }

    return appUser;

  }

  private AppUser createGoogleUser(OAuth2User oauth2User) {
    String email = extractEmail(oauth2User.getAttributes());
    String name = extractName(oauth2User.getAttributes());

    AppUser appUser = AppUser.builder()
        .name(name)
        .email(email)
        .address("")
        .role(Role.ACCOUNT_HOLDER)
        .approvalStatus(ApprovalStatus.PENDING)
        .build();

    return appUserRepository.save(appUser);
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

  private String extractName(Map<String, Object> attributes) {
    Object nameAttribute = attributes.get("name");

    if (nameAttribute instanceof String name && !name.isBlank()) {
      return name;
    }

    return "Google User";
  }

}