package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response object for a completed withdrawal")
public record WithdrawResponse(
        @Schema(description = "Unique ledger transaction ID", example = "f56g7h89-01cd-ef23-4567-8901abcdef23")
        UUID transactionId,
        
        @Schema(description = "Final status of transaction", example = "COMPLETED")
        String status,
        
        @Schema(description = "New wallet balance after withdrawal", example = "24.50")
        BigDecimal newBalance
) {}
