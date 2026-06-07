package com.stripe.payflow.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request payload to deposit funds")
public record DepositRequest(
        @NotNull(message = "Wallet ID is required")
        @Schema(description = "Target wallet UUID", example = "b12a3c45-d678-9012-e345-6789f0123456")
        UUID targetWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Schema(description = "Amount to deposit", example = "150.00")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Schema(description = "Currency code of the deposit", example = "USD")
        String currency
) {}
