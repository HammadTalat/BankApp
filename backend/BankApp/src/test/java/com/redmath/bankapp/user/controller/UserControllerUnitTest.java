package com.redmath.bankapp.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.redmath.bankapp.user.dto.request.CompleteProfileRequest;
import com.redmath.bankapp.user.dto.response.UserProfileResponse;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

  private static final String EMAIL = "user@example.com";

  @Mock
  private UserProfileService userProfileService;

  @InjectMocks
  private UserController userController;

  private Jwt jwtFor(String email) {
    return Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .subject(email)
        .build();
  }

  private UserProfileResponse sampleProfile() {
    return new UserProfileResponse(
        "Profile User",
        EMAIL,
        "123 Main St",
        Role.ACCOUNT_HOLDER,
        ApprovalStatus.PENDING
    );
  }

  @Nested
  @DisplayName("getCurrentUserProfile")
  class GetProfileTests {

    @Test
    @DisplayName("Should delegate to service using JWT subject and return profile")
    void getCurrentUserProfile_ReturnsProfile() {
      Jwt jwt = jwtFor(EMAIL);
      UserProfileResponse profile = sampleProfile();
      given(userProfileService.getCurrentUserProfile(EMAIL)).willReturn(profile);

      ResponseEntity<UserProfileResponse> response =
          userController.getCurrentUserProfile(jwt);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(profile);

      verify(userProfileService).getCurrentUserProfile(EMAIL);
      verifyNoMoreInteractions(userProfileService);
    }
  }

  @Nested
  @DisplayName("completeProfile")
  class CompleteProfileTests {

    @Test
    @DisplayName("Should delegate to service with JWT subject and address")
    void completeProfile_ValidAddress_ReturnsUpdatedProfile() {
      Jwt jwt = jwtFor(EMAIL);
      CompleteProfileRequest request = new CompleteProfileRequest("456 New Address Blvd");
      UserProfileResponse updatedProfile = new UserProfileResponse(
          "Profile User",
          EMAIL,
          "456 New Address Blvd",
          Role.ACCOUNT_HOLDER,
          ApprovalStatus.PENDING
      );
      given(userProfileService.completeProfile(EMAIL, request.address()))
          .willReturn(updatedProfile);

      ResponseEntity<UserProfileResponse> response =
          userController.completeProfile(jwt, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(updatedProfile);

      verify(userProfileService).completeProfile(EMAIL, request.address());
      verifyNoMoreInteractions(userProfileService);
    }
  }
}
