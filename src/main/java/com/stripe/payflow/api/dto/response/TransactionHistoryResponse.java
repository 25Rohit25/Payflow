package com.stripe.payflow.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object for a single ledger transaction history entry")
public record TransactionHistoryResponse(
        @Schema(description = "Ledger entry UUID", example = "g67h8i90-12de-f345-6789-0123abcdef45")
        UUID entryId,
        
        @Schema(description = "Ledger transaction UUID", example = "e45f6g78-90bc-def1-2345-67890abcdef1")
        UUID transactionId,
        
        @Schema(description = "Type of transaction", example = "TRANSFER")
        String transactionType,
        
        @Schema(description = "Direction of money movement for this wallet", example = "DEBIT")
        String direction,
        
        @Schema(description = "Amount moved", example = "50.00")
        BigDecimal amount,
        
        @Schema(description = "Currency", example = "USD")
        String currency,
        
        @Schema(description = "Timestamp of transaction", example = "2026-06-07T10:15:00Z")
        LocalDateTime createdAt
) {}
