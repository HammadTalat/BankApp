package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class ApiSecurityService {

  /**
   * Token validity in seconds.
   */
  public static final long TOKEN_EXPIRATION_SECONDS = 3600;

  private NimbusJwtEncoder jwtEncoder;

  private NimbusJwtDecoder jwtDecoder;

  @Value("${jwt.public.key}")
  private RSAPublicKey publicKey;

  @Value("${jwt.private.key}")
  private RSAPrivateKey privateKey;

  @PostConstruct
  public void initialize() {

    try {

      jwtEncoder = NimbusJwtEncoder
          .withKeyPair(publicKey, privateKey)
          .algorithm(SignatureAlgorithm.PS256)
          .build();

      jwtDecoder = NimbusJwtDecoder
          .withPublicKey(publicKey)
          .signatureAlgorithm(SignatureAlgorithm.PS256)
          .build();

    } catch (Exception exception) {

      throw new IllegalStateException(
          "Unable to initialize JWT infrastructure.",
          exception
      );

    }

  }

  /**
   * Exposes Spring Security JWT decoder.
   */
  public JwtDecoder jwtDecoder() {
    return jwtDecoder;
  }

  /**
   * Generates JWT for authenticated user.
   */
  public String generateToken(AppUser appUser) {

    Instant now = Instant.now();

    String scope = "ROLE_" + appUser.getRole().name();

    JwtClaimsSet claims = JwtClaimsSet.builder()

        .subject(appUser.getEmail())

        .issuedAt(now)

        .expiresAt(now.plusSeconds(TOKEN_EXPIRATION_SECONDS))

        .claim("scope", scope)

        .claim("userId", appUser.getId())

        .claim("jti", UUID.randomUUID().toString())

        .build();

    return jwtEncoder.encode(

        JwtEncoderParameters.from(

            JwsHeader.with(SignatureAlgorithm.PS256).build(),

            claims

        )

    ).getTokenValue();

  }

  /**
   * Decodes and validates JWT.
   */
  public Jwt decodeToken(String token) {
    return jwtDecoder.decode(token);
  }

}