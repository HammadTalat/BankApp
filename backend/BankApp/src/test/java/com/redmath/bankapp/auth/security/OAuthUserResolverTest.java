package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OAuthUserResolverTest {

  private AppUserRepository appUserRepository;
  private OAuthUserResolver oAuthUserResolver;

  @BeforeEach
  void setUp() {
    appUserRepository = mock(AppUserRepository.class);
    oAuthUserResolver = new OAuthUserResolver(appUserRepository);
  }

  @Test
  @DisplayName("Should return existing user when email is found")
  void resolve_ExistingUser_ReturnsExistingUser() {
    AppUser existingUser = AppUser.builder()
        .id(1L)
        .name("Existing User")
        .email("existing@example.com")
        .address("Existing Address")
        .role(Role.ACCOUNT_HOLDER)
        .approvalStatus(ApprovalStatus.PENDING)
        .build();

    given(appUserRepository.findByEmail("existing@example.com"))
        .willReturn(Optional.of(existingUser));

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "existing@example.com");
    attributes.put("name", "Existing User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    AppUser result = oAuthUserResolver.resolve(oauth2User);

    assertThat(result).isEqualTo(existingUser);
    verify(appUserRepository).findByEmail("existing@example.com");
  }

  @Test
  @DisplayName("Should create new Google user when email is not found")
  void resolve_NewUser_CreatesGoogleUser() {
    given(appUserRepository.findByEmail("newuser@example.com"))
        .willReturn(Optional.empty());
    given(appUserRepository.save(any(AppUser.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "newuser@example.com");
    attributes.put("name", "New User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    AppUser result = oAuthUserResolver.resolve(oauth2User);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("newuser@example.com");
    assertThat(result.getName()).isEqualTo("New User");
    assertThat(result.getRole()).isEqualTo(Role.ACCOUNT_HOLDER);
    assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
    assertThat(result.getAddress()).isEqualTo("Not provided");

    verify(appUserRepository).save(any(AppUser.class));
  }

  @Test
  @DisplayName("Should create new Google user with default name when name is missing")
  void resolve_NewUser_MissingName_UsesDefault() {
    given(appUserRepository.findByEmail("noname@example.com"))
        .willReturn(Optional.empty());
    given(appUserRepository.save(any(AppUser.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "noname@example.com");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    AppUser result = oAuthUserResolver.resolve(oauth2User);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Google User");
    assertThat(result.getEmail()).isEqualTo("noname@example.com");
  }

  @Test
  @DisplayName("Should throw OAuth2AuthenticationException when email is missing")
  void resolve_MissingEmail_ThrowsOAuth2Exception() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("name", "No Email User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "name"
    );

    assertThatThrownBy(() -> oAuthUserResolver.resolve(oauth2User))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessage("OAuth2 provider did not return an email address.");
  }

  @Test
  @DisplayName("Should throw OAuth2AuthenticationException when email is blank")
  void resolve_BlankEmail_ThrowsOAuth2Exception() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "   ");
    attributes.put("name", "Blank Email User");
    OAuth2User oauth2User = new DefaultOAuth2User(
        Collections.emptySet(),
        attributes,
        "email"
    );

    assertThatThrownBy(() -> oAuthUserResolver.resolve(oauth2User))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessage("OAuth2 provider did not return an email address.");
  }
}
