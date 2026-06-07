package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response object for a completed transfer")
public record TransferResponse(
        @Schema(description = "Unique ledger transaction ID", example = "e45f6g78-90bc-def1-2345-67890abcdef1")
        UUID transactionId,
        
        @Schema(description = "Final status of transaction", example = "COMPLETED")
        String status,
        
        @Schema(description = "Source wallet balance after transfer", example = "100.00")
        BigDecimal sourceWalletBalance,
        
        @Schema(description = "Target wallet balance after transfer", example = "150.00")
        BigDecimal targetWalletBalance
) {}
