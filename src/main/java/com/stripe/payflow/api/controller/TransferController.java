package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.api.dto.response.TransferResponse;
import com.stripe.payflow.application.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Transfer Funds", description = "Peer-to-peer asset transfer between two wallets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer completed successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request or inactive wallet",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict due to concurrent request with same Idempotency-Key",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Currency mismatch or Insufficient Funds",
                    content = @Content)
    })
    @PostMapping
    @RateLimiter(name = "transferRateLimiter")
    @CircuitBreaker(name = "transferCircuitBreaker")
    public ResponseEntity<TransferResponse> transfer(
            @Parameter(description = "Idempotency key to safely retry the request") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        return new ResponseEntity<>(transferService.transfer(request, idempotencyKey), HttpStatus.CREATED);
    }
}
