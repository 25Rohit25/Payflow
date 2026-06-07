package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.request.CreateWalletRequest;
import com.stripe.payflow.api.dto.response.WalletBalanceResponse;
import com.stripe.payflow.api.dto.response.WalletResponse;
import com.stripe.payflow.application.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management APIs")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Create Wallet", description = "Creates a new wallet for an existing user in the specified currency.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WalletResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input or User already has wallet in this currency",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return new ResponseEntity<>(walletService.createWallet(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public WalletResponse getWallet(@PathVariable UUID id) {
        return walletService.getWallet(id);
    }

    @Operation(summary = "Get Wallet Balance", description = "Retrieves the current balance and status of a wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WalletBalanceResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Wallet not found",
                    content = @Content)
    })
    @GetMapping("/{id}/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(walletService.getBalance(id));
    }
}
