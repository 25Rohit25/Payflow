package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.api.dto.response.DepositResponse;
import com.stripe.payflow.application.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment Operations (Deposits, Withdrawals, Transfers)")
public class DepositController {

    private final DepositService depositService;

    @Operation(summary = "Deposit Funds", description = "Deposit funds into a wallet via an external payment source.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deposit completed successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DepositResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request or inactive wallet",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict due to concurrent request with same Idempotency-Key",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Currency mismatch",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<DepositResponse> deposit(
            @Parameter(description = "Idempotency key to safely retry the request") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request) {
        return new ResponseEntity<>(depositService.deposit(request, idempotencyKey), HttpStatus.CREATED);
    }
}
