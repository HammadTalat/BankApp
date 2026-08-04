package com.redmath.bankapp.admin.service;

import com.redmath.bankapp.admin.dto.response.UserRejectionResponse;
import com.redmath.bankapp.admin.exception.InvalidUserStateException;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class    AdminRejectionService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public UserRejectionResponse rejectUser(Long userId) {
        AppUser user = findUser(userId);

        validateUserForRejection(user);

        user.setApprovalStatus(ApprovalStatus.REJECTED);

        AppUser rejectedUser = appUserRepository.save(user);

        return toResponse(rejectedUser);
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId
                ));
    }

    private void validateUserForRejection(AppUser user) {
        if (user.getRole() != Role.ACCOUNT_HOLDER) {
            throw new InvalidUserStateException(
                    "Only account holders can be rejected"
            );
        }

        if (user.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new InvalidUserStateException(
                    "Only pending users can be rejected"
            );
        }
    }

    private UserRejectionResponse toResponse(AppUser user) {
        return new UserRejectionResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getApprovalStatus()
        );
    }
}
