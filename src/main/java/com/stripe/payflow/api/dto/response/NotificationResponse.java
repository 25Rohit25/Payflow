package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object for a user notification")
public record NotificationResponse(
        @Schema(description = "Notification UUID", example = "h78i9j01-23ef-g456-7890-1234abcdef56")
        UUID id,
        
        @Schema(description = "Owning Wallet UUID", example = "b12a3c45-d678-9012-e345-6789f0123456")
        UUID walletId,
        
        @Schema(description = "Notification message text", example = "You received 50.00 USD.")
        String message,
        
        @Schema(description = "True if the notification has been acknowledged", example = "false")
        boolean read,
        
        @Schema(description = "Timestamp of notification", example = "2026-06-07T10:15:02Z")
        LocalDateTime createdAt
) {}
