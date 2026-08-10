package com.redmath.bankapp.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.bankapp.admin.dto.request.UpdateUserRequest;
import com.redmath.bankapp.admin.dto.response.AdminUserResponse;
import com.redmath.bankapp.admin.exception.DuplicateEmailException;
import com.redmath.bankapp.admin.exception.InvalidUpdateRequestException;
import com.redmath.bankapp.admin.exception.InvalidUserStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminUserServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "ali@example.com";

    private AppUserRepository appUserRepository;
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        adminUserService = new AdminUserService(appUserRepository);
    }

    @Test
    void shouldReturnPendingAccountHolders() {
        AppUser ali = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.PENDING
        );
        AppUser sara = createAccountHolder(
                2L,
                "Sara Ahmed",
                "sara@example.com",
                ApprovalStatus.PENDING
        );
        when(appUserRepository.findAllByRoleAndApprovalStatusOrderByIdAsc(
                Role.ACCOUNT_HOLDER,
                ApprovalStatus.PENDING
        )).thenReturn(List.of(ali, sara));

        List<AdminUserResponse> result =
                adminUserService.getUsersByApprovalStatus(
                        ApprovalStatus.PENDING
                );

        assertEquals(2, result.size());
        assertEquals(USER_ID, result.get(0).id());
        assertEquals("Ali Khan", result.get(0).name());
        assertEquals(EMAIL, result.get(0).email());
        assertEquals("Lahore", result.get(0).address());
        assertEquals(ApprovalStatus.PENDING, result.get(0).approvalStatus());
        assertEquals("Sara Ahmed", result.get(1).name());
        verify(appUserRepository)
                .findAllByRoleAndApprovalStatusOrderByIdAsc(
                        Role.ACCOUNT_HOLDER,
                        ApprovalStatus.PENDING
                );
    }

    @Test
    void shouldGetAccountHolderById() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        AdminUserResponse result = adminUserService.getUserById(USER_ID);

        assertEquals(USER_ID, result.id());
        assertEquals("Ali Khan", result.name());
        assertEquals(EMAIL, result.email());
        assertEquals("Lahore", result.address());
        assertEquals(ApprovalStatus.APPROVED, result.approvalStatus());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.getUserById(USER_ID)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
    }

    @Test
    void shouldUpdateNameEmailAndAddress() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(
                "  Ali Raza  ",
                "  ALI.RAZA@EXAMPLE.COM  ",
                "  Islamabad  "
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(appUserRepository.existsByEmailIgnoreCaseAndIdNot(
                "ali.raza@example.com",
                USER_ID
        )).thenReturn(false);
        when(appUserRepository.save(user)).thenReturn(user);

        AdminUserResponse result = adminUserService.updateUser(
                USER_ID,
                request
        );

        assertEquals("Ali Raza", user.getName());
        assertEquals("ali.raza@example.com", user.getEmail());
        assertEquals("Islamabad", user.getAddress());
        assertEquals("Ali Raza", result.name());
        assertEquals("ali.raza@example.com", result.email());
        assertEquals("Islamabad", result.address());
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldSupportChangingOnlyOneField() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(
                "  Ali Raza  ",
                null,
                null
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(user);

        AdminUserResponse result = adminUserService.updateUser(
                USER_ID,
                request
        );

        assertEquals("Ali Raza", result.name());
        assertEquals(EMAIL, result.email());
        assertEquals("Lahore", result.address());
        verify(appUserRepository, never())
                .existsByEmailIgnoreCaseAndIdNot(
                        anyString(),
                        anyLong()
                );
    }

    @Test
    void shouldAllowUserToKeepSameEmail() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(
                null,
                "  ALI@EXAMPLE.COM  ",
                null
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(user);

        AdminUserResponse result = adminUserService.updateUser(
                USER_ID,
                request
        );

        assertEquals(EMAIL, result.email());
        verify(appUserRepository, never())
                .existsByEmailIgnoreCaseAndIdNot(
                        anyString(),
                        anyLong()
                );
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(
                null,
                "sara@example.com",
                null
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(appUserRepository.existsByEmailIgnoreCaseAndIdNot(
                "sara@example.com",
                USER_ID
        )).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals("Email is already registered", exception.getMessage());
        assertEquals(EMAIL, user.getEmail());
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldRejectEmptyUpdateRequest() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null);

        InvalidUpdateRequestException exception = assertThrows(
                InvalidUpdateRequestException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals(
                "At least one field must be provided",
                exception.getMessage()
        );
        verify(appUserRepository, never()).findById(USER_ID);
    }

    @Test
    void shouldRejectBlankName() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest("   ", null, null);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUpdateRequestException exception = assertThrows(
                InvalidUpdateRequestException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals("Name cannot be empty", exception.getMessage());
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldRejectBlankEmail() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(null, "   ", null);
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUpdateRequestException exception = assertThrows(
                InvalidUpdateRequestException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals("Email cannot be empty", exception.getMessage());
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldRejectBlankAddress() {
        AppUser user = createAccountHolder(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(null, null, "   ");
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        InvalidUpdateRequestException exception = assertThrows(
                InvalidUpdateRequestException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals("Address cannot be empty", exception.getMessage());
        verify(appUserRepository, never()).save(user);
    }

    @Test
    void shouldRejectEditingAdminThroughAccountHolderEndpoint() {
        AppUser admin = new AppUser(
                USER_ID,
                "Bank Admin",
                "admin@example.com",
                "Head Office",
                Role.ADMIN,
                ApprovalStatus.APPROVED
        );
        UpdateUserRequest request = new UpdateUserRequest(
                "New Name",
                null,
                null
        );
        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(admin));

        InvalidUserStateException exception = assertThrows(
                InvalidUserStateException.class,
                () -> adminUserService.updateUser(USER_ID, request)
        );

        assertEquals(
                "Only account-holder details can be updated",
                exception.getMessage()
        );
        assertEquals(Role.ADMIN, admin.getRole());
        verify(appUserRepository, never()).save(admin);
    }

    private AppUser createAccountHolder(
            Long id,
            String name,
            String email,
            ApprovalStatus approvalStatus
    ) {
        return new AppUser(
                id,
                name,
                email,
                "Lahore",
                Role.ACCOUNT_HOLDER,
                approvalStatus
        );
    }
}
