package com.redmath.bankapp.user.controller;

import com.redmath.bankapp.user.dto.request.CompleteProfileRequest;
import com.redmath.bankapp.user.dto.response.UserProfileResponse;
import com.redmath.bankapp.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {

  private final UserProfileService userProfileService;

  @GetMapping
  public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
      @AuthenticationPrincipal Jwt jwt) {

    return ResponseEntity.ok(
        userProfileService.getCurrentUserProfile(jwt.getSubject())
    );
  }

  @PostMapping("/complete-profile")
  public ResponseEntity<UserProfileResponse> completeProfile(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody CompleteProfileRequest request) {

    return ResponseEntity.ok(
        userProfileService.completeProfile(jwt.getSubject(), request.address())
    );
  }

}