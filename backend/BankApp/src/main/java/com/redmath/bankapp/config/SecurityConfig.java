package com.redmath.bankapp.config;

import com.redmath.bankapp.auth.security.ApiSecurityService;
import com.redmath.bankapp.auth.security.ApiAuthenticationFailureHandler;
import com.redmath.bankapp.auth.security.ApiAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final ApiAuthenticationSuccessHandler apiAuthenticationSuccessHandler;
  private final ApiAuthenticationFailureHandler authenticationFailureHandler;
  private final ApiSecurityService apiSecurityService;

  public SecurityConfig(
      ApiAuthenticationSuccessHandler apiAuthenticationSuccessHandler,
      ApiAuthenticationFailureHandler authenticationFailureHandler,
      ApiSecurityService apiSecurityService) {

    this.apiAuthenticationSuccessHandler = apiAuthenticationSuccessHandler;
    this.authenticationFailureHandler = authenticationFailureHandler;
    this.apiSecurityService = apiSecurityService;
  }

  @Bean
  AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration)
      throws Exception {

    return configuration.getAuthenticationManager();

  }
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  @Bean
  JwtDecoder jwtDecoder() {
    return apiSecurityService.jwtDecoder();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    http

        .csrf(csrf -> csrf.disable())

        .sessionManagement(session ->

            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth

            .requestMatchers(

                "/swagger-ui/**",

                "/v3/api-docs/**"

            ).permitAll()

            .requestMatchers(

                "/oauth2/**",

                "/login/oauth2/**"

            ).permitAll()

            .requestMatchers(

                HttpMethod.POST,

                "/api/v1/auth/signup",

              "/api/v1/auth/login",

              "/api/v1/auth/logout"

            ).permitAll()

            .anyRequest()

            .authenticated()

        )

        .formLogin(form -> form

            .loginProcessingUrl("/api/v1/auth/login")

            .successHandler(apiAuthenticationSuccessHandler)

            .failureHandler(authenticationFailureHandler)

        )

        .oauth2Login(Customizer.withDefaults())

        .oauth2ResourceServer(resource ->

            resource.jwt(Customizer.withDefaults())

        );

    return http.build();

  }

}