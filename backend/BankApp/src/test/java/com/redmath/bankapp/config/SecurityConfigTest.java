package com.redmath.bankapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.auth.security.ApiAuthenticationFailureHandler;
import com.redmath.bankapp.auth.security.ApiAuthenticationSuccessHandler;
import com.redmath.bankapp.auth.security.ApiSecurityService;
import com.redmath.bankapp.auth.security.JwtAuthenticationFilter;
import com.redmath.bankapp.auth.security.OAuth2SuccessHandler;
import com.redmath.bankapp.auth.security.PendingProfileAccessManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    private ApiSecurityService apiSecurityService;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        apiSecurityService = mock(ApiSecurityService.class);
        securityConfig = new SecurityConfig(
                mock(ApiAuthenticationSuccessHandler.class),
                mock(ApiAuthenticationFailureHandler.class),
                mock(OAuth2SuccessHandler.class),
                apiSecurityService,
                mock(JwtAuthenticationFilter.class),
                mock(PendingProfileAccessManager.class)
        );
        ReflectionTestUtils.setField(
                securityConfig,
                "frontendUrl",
                "http://frontend.redmath.test"
        );
    }

    @Test
    void corsConfigurationAllowsOnlyTheConfiguredFrontendAndRequiredHttpDetails() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://frontend.redmath.test");
        assertThat(cors.getAllowedMethods())
                .containsExactly("GET", "POST", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    void exposesTheExpectedSecurityBeans() throws Exception {
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
        when(apiSecurityService.jwtDecoder()).thenReturn(decoder);

        assertThat(securityConfig.authenticationManager(authenticationConfiguration))
                .isSameAs(authenticationManager);
        assertThat(securityConfig.jwtDecoder()).isSameAs(decoder);
        assertThat(securityConfig.objectMapper()).isNotNull();

        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String hash = encoder.encode("Banking-password-123!");
        assertThat(hash).isNotEqualTo("Banking-password-123!");
        assertThat(encoder.matches("Banking-password-123!", hash)).isTrue();
    }
}
