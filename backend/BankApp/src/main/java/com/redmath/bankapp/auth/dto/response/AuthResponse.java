package com.redmath.bankapp.auth.dto.response;

public record AuthResponse(

    String accessToken,

    String tokenType,

    long expiresIn,

    String email,

    String name,

    String role,

    String redirectPath

) {
}