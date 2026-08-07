package com.redmath.bankapp.auth.repository;

import com.redmath.bankapp.auth.entity.LocalCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, String> {

  Optional<LocalCredential> findByEmail(String email);

  boolean existsByEmail(String email);

}