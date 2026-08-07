package com.redmath.bankapp.admin.dto.response;


import com.redmath.bankapp.user.entity.ApprovalStatus;

public record AdminUserResponse(
    Long id,
    String name,
    String email,
    String address,
    ApprovalStatus approvalStatus
) {

}