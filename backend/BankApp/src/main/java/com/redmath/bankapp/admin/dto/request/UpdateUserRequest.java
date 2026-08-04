package com.redmath.bankapp.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Email(message = "Email format is invalid")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address

) {
}