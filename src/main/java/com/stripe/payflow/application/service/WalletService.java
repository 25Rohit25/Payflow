package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.CreateWalletRequest;
import com.stripe.payflow.api.dto.response.WalletBalanceResponse;
import com.stripe.payflow.api.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletService {
    WalletResponse createWallet(CreateWalletRequest request);
    WalletResponse getWallet(UUID id);
    WalletBalanceResponse getBalance(UUID id);
}
