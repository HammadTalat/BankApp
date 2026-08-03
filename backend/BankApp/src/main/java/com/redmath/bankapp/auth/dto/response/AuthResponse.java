package com.redmath.bankapp.auth.dto.response;

import com.redmath.bankapp.user.entity.Role;

public record AuthResponse(

    String accessToken,

    String tokenType,

    Long expiresIn,

    String name,

    String email,

    Role role

) {
}