package com.stripe.payflow.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to create a new user")
public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        @Schema(description = "Full name of the user", example = "Jane Doe")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Valid email address", example = "jane.doe@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "Secure password", example = "SecurePassword123!")
        String password
) {}
