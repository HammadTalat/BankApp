package com.redmath.bankapp.auth.repository;

import com.redmath.bankapp.auth.entity.LocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, String> {

  Optional<LocalCredential> findByEmail(String email);

  boolean existsByEmail(String email);

}