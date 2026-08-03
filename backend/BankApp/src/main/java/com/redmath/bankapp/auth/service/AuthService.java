package com.redmath.bankapp.auth.service;

import com.redmath.bankapp.auth.dto.request.SignupRequest;
import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.exception.DuplicateEmailException;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AppUserRepository appUserRepository;
  private final LocalCredentialRepository localCredentialRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public SignupResponse signup(SignupRequest request) {

    if (appUserRepository.existsByEmail(request.email())) {
      throw new DuplicateEmailException(
          "Email already exists: " + request.email());
    }

    AppUser appUser = AppUser.builder()
        .name(request.name())
        .email(request.email())
        .address(request.address())
        .role(Role.ACCOUNT_HOLDER)
        .approvalStatus(ApprovalStatus.PENDING)
        .build();

    LocalCredential credential = LocalCredential.builder()
        .email(request.email())
        .passwordHash(passwordEncoder.encode(request.password()))
        .build();

    appUserRepository.save(appUser);
    localCredentialRepository.save(credential);

    return new SignupResponse(
        true,
        "Registration submitted successfully. Your account is pending administrator approval."
    );
  }

}