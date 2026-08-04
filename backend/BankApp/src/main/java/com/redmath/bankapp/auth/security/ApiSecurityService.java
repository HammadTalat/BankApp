package com.redmath.bankapp.auth.security;

import com.redmath.bankapp.user.entity.AppUser;
import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
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

  @PostConstruct
  public void initialize() {

    try {

      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

      keyPairGenerator.initialize(2048);

      KeyPair keyPair = keyPairGenerator.generateKeyPair();

      RSAPublicKey publicKey =
          (RSAPublicKey) keyPair.getPublic();

      RSAPrivateKey privateKey =
          (RSAPrivateKey) keyPair.getPrivate();

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
  public String generateToken(CustomUserDetails userDetails) {

    Instant now = Instant.now();

    AppUser appUser = userDetails.getAppUser();

    String scope = userDetails.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));

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