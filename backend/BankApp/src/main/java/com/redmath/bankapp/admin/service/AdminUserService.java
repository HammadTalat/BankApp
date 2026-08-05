package com.redmath.bankapp.admin.service;


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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsersByApprovalStatus(
            ApprovalStatus approvalStatus
    ) {
        return appUserRepository
                .findAllByRoleAndApprovalStatusOrderByIdAsc(
                        Role.ACCOUNT_HOLDER,
                        approvalStatus
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        AppUser user = findAccountHolder(userId);

        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ) {
        validateRequest(request);

        AppUser user = findAccountHolder(userId);

        updateName(user, request.name());
        updateEmail(user, request.email());
        updateAddress(user, request.address());

        AppUser updatedUser = appUserRepository.save(user);

        return toResponse(updatedUser);
    }

    private AppUser findAccountHolder(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId
                ));

        if (user.getRole() != Role.ACCOUNT_HOLDER) {
            throw new InvalidUserStateException(
                    "Only account-holder details can be updated"
            );
        }

        return user;
    }

    private void validateRequest(UpdateUserRequest request) {
        if (request.name() == null
                && request.email() == null
                && request.address() == null) {

            throw new InvalidUpdateRequestException(
                    "At least one field must be provided"
            );
        }
    }

    private void updateName(AppUser user, String name) {
        if (name == null) {
            return;
        }

        String normalizedName = requireText(name, "Name");

        user.setName(normalizedName);
    }

    private void updateEmail(AppUser user, String email) {
        if (email == null) {
            return;
        }

        String normalizedEmail = requireText(email, "Email")
                .toLowerCase(Locale.ROOT);

        boolean emailChanged =
                !user.getEmail().equalsIgnoreCase(normalizedEmail);

        if (!emailChanged) {
            return;
        }

        boolean emailExists =
                appUserRepository.existsByEmailIgnoreCaseAndIdNot(
                        normalizedEmail,
                        user.getId()
                );

        if (emailExists) {
            throw new DuplicateEmailException(
                    "Email is already registered"
            );
        }

        user.setEmail(normalizedEmail);
    }

    private void updateAddress(AppUser user, String address) {
        if (address == null) {
            return;
        }

        String normalizedAddress = requireText(
                address,
                "Address"
        );

        user.setAddress(normalizedAddress);
    }

    private String requireText(String value, String fieldName) {
        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new InvalidUpdateRequestException(
                    fieldName + " cannot be empty"
            );
        }

        return normalizedValue;
    }


    private AdminUserResponse toResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAddress(),
                user.getApprovalStatus()
        );
    }
}