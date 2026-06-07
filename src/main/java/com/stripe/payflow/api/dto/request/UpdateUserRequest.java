package com.stripe.payflow.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "Request payload to update an existing user")
public record UpdateUserRequest(
        @Schema(description = "Updated full name of the user", example = "John Smith")
        String name,

        @Email(message = "Invalid email format")
        @Schema(description = "Updated email address", example = "john.smith@example.com")
        String email,

        @Schema(description = "Updated secure password", example = "NewSecurePassword123!")
        String password
) {}
