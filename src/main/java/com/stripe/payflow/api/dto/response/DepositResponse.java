package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response object for a completed deposit")
public record DepositResponse(
        @Schema(description = "Unique ledger transaction ID", example = "d34e5f67-89ab-cdef-0123-456789abcdef")
        UUID transactionId,
        
        @Schema(description = "Final status of transaction", example = "COMPLETED")
        String status,
        
        @Schema(description = "New wallet balance after deposit", example = "250.00")
        BigDecimal newBalance
) {}
