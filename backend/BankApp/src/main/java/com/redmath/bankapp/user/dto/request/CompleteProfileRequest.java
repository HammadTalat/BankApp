package com.redmath.bankapp.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteProfileRequest(
    @NotBlank(message = "Address is required.")
    String address
) {

}
