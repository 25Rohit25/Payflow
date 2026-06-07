package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response object containing real-time wallet balance")
public record WalletBalanceResponse(
        @Schema(description = "Wallet UUID", example = "b12a3c45-d678-9012-e345-6789f0123456")
        UUID walletId,
        
        @Schema(description = "Wallet currency", example = "USD")
        String currency,
        
        @Schema(description = "Current balance", example = "1050.75")
        BigDecimal balance,
        
        @Schema(description = "Current operational status", example = "ACTIVE")
        String status
) {}
