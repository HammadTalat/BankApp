package com.redmath.bankapp.admin.dto.response;


import com.redmath.bankapp.user.entity.ApprovalStatus;

public record UserRejectionResponse(
    Long userId,
    String name,
    String email,
    ApprovalStatus approvalStatus
) {

}