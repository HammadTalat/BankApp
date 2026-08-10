package com.redmath.bankapp.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.redmath.bankapp.user.dto.response.UserProfileResponse;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @InjectMocks
  private UserProfileService userProfileService;

  private AppUser appUser;
  private static final String EMAIL = "user@example.com";

  @BeforeEach
  void setUp() {
    appUser = AppUser.builder()
        .id(1L)
        .name("Profile User")
        .email(EMAIL)
        .address("Original Address")
        .role(Role.ACCOUNT_HOLDER)
        .approvalStatus(ApprovalStatus.PENDING)
        .build();
  }

  @Nested
  @DisplayName("getCurrentUserProfile")
  class GetCurrentUserProfileTests {

    @Test
    @DisplayName("Should return profile for existing user")
    void getCurrentUserProfile_UserExists_ReturnsProfile() {
      given(appUserRepository.findByEmail(EMAIL)).willReturn(Optional.of(appUser));

      UserProfileResponse response = userProfileService.getCurrentUserProfile(EMAIL);

      assertThat(response.name()).isEqualTo("Profile User");
      assertThat(response.email()).isEqualTo(EMAIL);
      assertThat(response.address()).isEqualTo("Original Address");
      assertThat(response.role()).isEqualTo(Role.ACCOUNT_HOLDER);
      assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.PENDING);

      verify(appUserRepository).findByEmail(EMAIL);
      verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    @DisplayName("Should throw when user is not found")
    void getCurrentUserProfile_UserNotFound_ThrowsRuntimeException() {
      given(appUserRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

      assertThatThrownBy(() -> userProfileService.getCurrentUserProfile(EMAIL))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User not found: " + EMAIL);

      verify(appUserRepository).findByEmail(EMAIL);
      verifyNoMoreInteractions(appUserRepository);
    }
  }

  @Nested
  @DisplayName("completeProfile")
  class CompleteProfileTests {

    @Test
    @DisplayName("Should update address and return updated profile")
    void completeProfile_ValidAddress_ReturnsUpdatedProfile() {
      given(appUserRepository.findByEmail(EMAIL)).willReturn(Optional.of(appUser));

      UserProfileResponse response =
          userProfileService.completeProfile(EMAIL, "456 New Address Blvd");

      assertThat(response.address()).isEqualTo("456 New Address Blvd");
      assertThat(response.name()).isEqualTo("Profile User");
      assertThat(response.email()).isEqualTo(EMAIL);

      verify(appUserRepository).findByEmail(EMAIL);
      verify(appUserRepository).save(appUser);
      verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    @DisplayName("Should reject blank address")
    void completeProfile_BlankAddress_ThrowsIllegalArgumentException() {
      given(appUserRepository.findByEmail(EMAIL)).willReturn(Optional.of(appUser));

      assertThatThrownBy(() -> userProfileService.completeProfile(EMAIL, "   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Address is required.");

      verify(appUserRepository).findByEmail(EMAIL);
      verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    @DisplayName("Should reject null address")
    void completeProfile_NullAddress_ThrowsIllegalArgumentException() {
      given(appUserRepository.findByEmail(EMAIL)).willReturn(Optional.of(appUser));

      assertThatThrownBy(() -> userProfileService.completeProfile(EMAIL, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Address is required.");

      verify(appUserRepository).findByEmail(EMAIL);
      verifyNoMoreInteractions(appUserRepository);
    }
  }
}
