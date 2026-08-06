package com.redmath.bankapp.tempconfig.security;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Mock security config parked temporarily.
// @Configuration
// @EnableWebSecurity
public class SecurityConfig {

    private final DevMockUserFilter devMockUserFilter;

    public SecurityConfig(DevMockUserFilter devMockUserFilter) {
        this.devMockUserFilter = devMockUserFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for easy API testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all endpoints without credentials
                )
                // Add the mock filter before Spring's authentication processing
                .addFilterBefore(devMockUserFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}