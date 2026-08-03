package com.redmath.bankapp.admin.service;


import com.redmath.bankapp.admin.dto.response.AdminUserResponse;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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