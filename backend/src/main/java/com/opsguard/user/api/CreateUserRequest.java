package com.opsguard.user.api;

import com.opsguard.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(
                max = 254,
                message = "Email must not exceed 254 characters."
        )
        String email,

        @NotBlank(message = "First name is required.")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters."
        )
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters."
        )
        String lastName,

        @NotNull(message = "Role is required.")
        UserRole role
) {
}