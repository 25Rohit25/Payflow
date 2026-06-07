package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.request.WithdrawRequest;
import com.stripe.payflow.api.dto.response.WithdrawResponse;
import com.stripe.payflow.application.service.WithdrawService;
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
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    @Operation(summary = "Withdraw Funds", description = "Withdraw funds from a wallet to an external destination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdrawal completed successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WithdrawResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request or inactive wallet",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict due to concurrent request with same Idempotency-Key",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Currency mismatch or Insufficient Funds",
                    content = @Content)
    })
    public ResponseEntity<WithdrawResponse> withdraw(
            @Parameter(description = "Idempotency key to safely retry the request") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(withdrawService.withdraw(request, idempotencyKey));
    }
}
