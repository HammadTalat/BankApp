package com.redmath.bankapp.user.dto.response;

import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;

public record UserProfileResponse(

    String name,

    String email,

    String address,

    Role role,

    ApprovalStatus approvalStatus

) {
}