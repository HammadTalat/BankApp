package com.redmath.bankapp.admin.controller;

import com.redmath.bankapp.admin.dto.request.UpdateUserRequest;
import com.redmath.bankapp.admin.dto.response.AdminUserResponse;
import com.redmath.bankapp.admin.dto.response.UserApprovalResponse;
import com.redmath.bankapp.admin.dto.response.UserRejectionResponse;
import com.redmath.bankapp.admin.service.AdminApprovalService;
import com.redmath.bankapp.admin.service.AdminRejectionService;
import com.redmath.bankapp.admin.service.AdminUserService;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminApprovalService adminApprovalService;
    private final AdminUserService adminUserService;
    private final AdminRejectionService adminRejectionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @RequestParam ApprovalStatus approvalStatus
    ) {
        List<AdminUserResponse> users =
                adminUserService.getUsersByApprovalStatus(approvalStatus);

        return ResponseEntity.ok(users);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                adminUserService.getUserById(userId)
        );
    }
    @PatchMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUser(userId, request)
        );
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserApprovalResponse> approveUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                adminApprovalService.approveUser(userId)
        );
    }
    @PostMapping("/{userId}/reject")
    public ResponseEntity<UserRejectionResponse> rejectUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                adminRejectionService.rejectUser(userId)
        );
    }
}