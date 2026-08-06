package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;
  private final LocalCredentialRepository localCredentialRepository;

  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    AppUser appUser = appUserRepository.findByEmail(email)
        .orElseThrow(() ->
            new UsernameNotFoundException(
                "User not found: " + email));

    LocalCredential credential =
        localCredentialRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Credentials not found for: " + email));

    return new CustomUserDetails(
        appUser,
        credential
    );
  }

}