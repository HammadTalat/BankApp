package com.redmath.bankapp.user.service;

import com.redmath.bankapp.user.dto.response.UserProfileResponse;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.exception.UserNotFoundException;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final AppUserRepository appUserRepository;

  public UserProfileResponse getCurrentUserProfile(String email) {
    AppUser appUser = appUserRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

    return new UserProfileResponse(
        appUser.getName(),
        appUser.getEmail(),
        appUser.getAddress(),
        appUser.getRole(),
        appUser.getApprovalStatus()
    );
  }

}