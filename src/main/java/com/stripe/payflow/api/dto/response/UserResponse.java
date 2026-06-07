package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object containing created user details")
public record UserResponse(
        @Schema(description = "User's unique UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Full name", example = "Jane Doe")
        String name,
        
        @Schema(description = "Email address", example = "jane.doe@example.com")
        String email,
        
        @Schema(description = "Timestamp of creation", example = "2026-06-07T10:00:00Z")
        LocalDateTime createdAt
) {}
