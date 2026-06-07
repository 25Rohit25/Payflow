package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object containing wallet details")
public record WalletResponse(
        @Schema(description = "Wallet UUID", example = "b12a3c45-d678-9012-e345-6789f0123456")
        UUID id,
        
        @Schema(description = "Owning user UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,
        
        @Schema(description = "Wallet currency", example = "USD")
        String currency,
        
        @Schema(description = "Current operational status", example = "ACTIVE")
        String status,
        
        @Schema(description = "Timestamp of creation", example = "2026-06-07T10:00:00Z")
        LocalDateTime createdAt
) {}
