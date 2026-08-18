package com.redmath.bankapp.tempconfig.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class TemporarySecuritySupportTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userPrincipalExposesTheProvidedIdentityAndAnEnabledAccountState() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ACCOUNT_HOLDER");
        UserPrincipal principal = new UserPrincipal(
                42L,
                "Ayesha Khan",
                "ayesha@example.com",
                List.of(authority)
        );

        assertThat(principal.getId()).isEqualTo(42L);
        assertThat(principal.getUsername()).isEqualTo("Ayesha Khan");
        assertThat(principal.getEmail()).isEqualTo("ayesha@example.com");
        assertThat(principal.getPassword()).isEmpty();
        assertThat(principal.getAuthorities())
                .extracting(grantedAuthority -> grantedAuthority.getAuthority())
                .containsExactly("ROLE_ACCOUNT_HOLDER");
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void developmentFilterAddsTheExpectedMockAccountHolderToTheSecurityContext() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        new DevMockUserFilter().doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertThat(principal.getId()).isEqualTo(3L);
        assertThat(principal.getUsername()).isEqualTo("John Doe Sender");
        assertThat(principal.getEmail()).isEqualTo("sender@redmath.com");
        assertThat(principal.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ACCOUNT_HOLDER");
        verify(chain).doFilter(request, response);
    }
}
