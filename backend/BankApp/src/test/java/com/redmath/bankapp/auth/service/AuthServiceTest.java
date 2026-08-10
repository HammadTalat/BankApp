package com.redmath.bankapp.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.redmath.bankapp.auth.dto.request.SignupRequest;
import com.redmath.bankapp.auth.dto.response.SignupResponse;
import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.exception.DuplicateEmailException;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private LocalCredentialRepository localCredentialRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  private SignupRequest validRequest() {
    return new SignupRequest(
        "Jane Doe",
        "jane@example.com",
        "123 Main St",
        "password123"
    );
  }

  @Nested
  @DisplayName("signup")
  class SignupTests {

    @Test
    @DisplayName("Should persist user and credential and return success response")
    void signup_ValidRequest_ReturnsSuccessResponse() {
      SignupRequest request = validRequest();
      given(appUserRepository.existsByEmail(request.email())).willReturn(false);
      given(passwordEncoder.encode(request.password())).willReturn("encoded-password");

      SignupResponse response = authService.signup(request);

      assertThat(response.success()).isTrue();
      assertThat(response.message())
          .contains("pending administrator approval");

      ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
      verify(appUserRepository).existsByEmail(request.email());
      verify(appUserRepository).save(userCaptor.capture());
      AppUser savedUser = userCaptor.getValue();
      assertThat(savedUser.getName()).isEqualTo(request.name());
      assertThat(savedUser.getEmail()).isEqualTo(request.email());
      assertThat(savedUser.getAddress()).isEqualTo(request.address());
      assertThat(savedUser.getRole()).isEqualTo(Role.ACCOUNT_HOLDER);
      assertThat(savedUser.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

      ArgumentCaptor<LocalCredential> credentialCaptor = ArgumentCaptor.forClass(LocalCredential.class);
      verify(localCredentialRepository).save(credentialCaptor.capture());
      LocalCredential savedCredential = credentialCaptor.getValue();
      assertThat(savedCredential.getEmail()).isEqualTo(request.email());
      assertThat(savedCredential.getPasswordHash()).isEqualTo("encoded-password");

      verify(passwordEncoder).encode(request.password());
      verifyNoMoreInteractions(appUserRepository, localCredentialRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void signup_DuplicateEmail_ThrowsDuplicateEmailException() {
      SignupRequest request = validRequest();
      given(appUserRepository.existsByEmail(request.email())).willReturn(true);

      assertThatThrownBy(() -> authService.signup(request))
          .isInstanceOf(DuplicateEmailException.class)
          .hasMessage("Email already exists: " + request.email());

      verify(appUserRepository).existsByEmail(request.email());
      verifyNoMoreInteractions(appUserRepository, localCredentialRepository, passwordEncoder);
    }
  }
}
