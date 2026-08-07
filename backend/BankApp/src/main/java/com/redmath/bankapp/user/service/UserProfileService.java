package com.redmath.bankapp.user.service;

import com.redmath.bankapp.user.dto.response.UserProfileResponse;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final AppUserRepository appUserRepository;

  public UserProfileResponse getCurrentUserProfile(String email) {
    AppUser appUser = findUser(email);

    return new UserProfileResponse(
        appUser.getName(),
        appUser.getEmail(),
        appUser.getAddress(),
        appUser.getRole(),
        appUser.getApprovalStatus()
    );
  }

  @Transactional
  public UserProfileResponse completeProfile(String email, String address) {
    AppUser appUser = findUser(email);

    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("Address is required.");
    }

    appUser.setAddress(address);
    appUserRepository.save(appUser);

    return new UserProfileResponse(
        appUser.getName(),
        appUser.getEmail(),
        appUser.getAddress(),
        appUser.getRole(),
        appUser.getApprovalStatus()
    );
  }

  private AppUser findUser(String email) {
    return appUserRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
  }

}