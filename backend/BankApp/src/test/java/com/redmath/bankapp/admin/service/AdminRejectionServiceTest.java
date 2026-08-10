package com.redmath.bankapp.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.admin.dto.response.UserRejectionResponse;
import com.redmath.bankapp.admin.exception.InvalidUserStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminRejectionServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "ali@example.com";

    private AppUserRepository appUserRepository;
    private AdminRejectionService adminRejectionService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        adminRejectionService = new AdminRejectionService(appUserRepository);
    }

    @Test
    void shouldRejectPendingUser() {
        AppUser user = createPendingAccountHolder();
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(user);

        UserRejectionResponse result =
                adminRejectionService.rejectUser(USER_ID);

        assertEquals(ApprovalStatus.REJECTED, user.getApprovalStatus());
        assertEquals(USER_ID, result.userId());
        assertEquals("Ali Khan", result.name());
        assertEquals(EMAIL, result.email());
        assertEquals(ApprovalStatus.REJECTED, result.approvalStatus());
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminRejectionService.rejectUser(USER_ID)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }

    @Test
    void shouldReturnErrorWhenUserIsAlreadyApproved() {
        AppUser user = createPendingAccountHolder();
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminRejectionService.rejectUser(USER_ID)
        );

        assertEquals(
                "Only pending users can be rejected",
                exception.getMessage()
        );
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldReturnErrorWhenUserIsAlreadyRejected() {
        AppUser user = createPendingAccountHolder();
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminRejectionService.rejectUser(USER_ID)
        );

        assertEquals(
                "Only pending users can be rejected",
                exception.getMessage()
        );
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldReturnErrorWhenUserIsNotAccountHolder() {
        AppUser admin = new AppUser(
                USER_ID,
                "Bank Admin",
                "admin@example.com",
                "Head Office",
                Role.ADMIN,
                ApprovalStatus.PENDING
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(admin));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminRejectionService.rejectUser(USER_ID)
        );

        assertEquals(
                "Only account holders can be rejected",
                exception.getMessage()
        );
        verify(appUserRepository, never()).save(admin);
    }

    private AppUser createPendingAccountHolder() {
        return new AppUser(
                USER_ID,
                "Ali Khan",
                EMAIL,
                "Lahore",
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.PENDING
        );
    }
}
