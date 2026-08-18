package com.redmath.bankapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

class PendingProfileAccessManagerTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final PendingProfileAccessManager manager = new PendingProfileAccessManager(users);

    @Test
    void rejectsMissingOrUnauthenticatedAuthentication() {
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);

        assertThat(manager.hasAccess(request("/api/v1/me"), null)).isFalse();
        assertThat(manager.hasAccess(request("/api/v1/me"), unauthenticated)).isFalse();
        verifyNoInteractions(users);
    }

    @Test
    void rejectsUnsupportedPrincipalAndUnknownUser() {
        Authentication unsupported = authenticatedWith(new Object());
        Authentication unknown = authenticatedWith("missing@redmath.test");
        when(users.findByEmail("missing@redmath.test")).thenReturn(Optional.empty());

        assertThat(manager.hasAccess(request("/api/v1/me"), unsupported)).isFalse();
        assertThat(manager.hasAccess(request("/api/v1/me"), unknown)).isFalse();
    }

    @Test
    void allowsApprovedJwtUserForEveryEndpoint() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
                .subject("approved@redmath.test").build();
        when(users.findByEmail("approved@redmath.test"))
                .thenReturn(Optional.of(user(ApprovalStatus.APPROVED)));

        assertThat(manager.hasAccess(request("/api/v1/transaction/transfer"), authenticatedWith(jwt)))
                .isTrue();
    }

    @Test
    void limitsPendingStringPrincipalToOwnProfileEndpoints() {
        when(users.findByEmail("pending@redmath.test"))
                .thenReturn(Optional.of(user(ApprovalStatus.PENDING)));
        Authentication authentication = authenticatedWith("pending@redmath.test");

        assertThat(manager.hasAccess(request("/api/v1/me"), authentication)).isTrue();
        assertThat(manager.hasAccess(request("/api/v1/me/profile"), authentication)).isTrue();
        assertThat(manager.hasAccess(request("/api/v1/transaction/deposit"), authentication)).isFalse();
    }

    private Authentication authenticatedWith(Object principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        return authentication;
    }

    private AppUser user(ApprovalStatus status) {
        return AppUser.builder()
                .email("user@redmath.test")
                .name("Test User")
                .address("Test address")
                .role(Role.ACCOUNT_HOLDER)
                .approvalStatus(status)
                .build();
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
