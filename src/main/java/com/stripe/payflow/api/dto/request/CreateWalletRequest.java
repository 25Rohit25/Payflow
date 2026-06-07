package com.stripe.payflow.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request payload to create a new wallet")
public record CreateWalletRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "Unique UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @NotBlank(message = "Currency is required")
        @Schema(description = "3-letter ISO currency code", example = "USD")
        String currency
) {}
