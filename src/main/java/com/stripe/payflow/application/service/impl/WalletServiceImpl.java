package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.request.CreateWalletRequest;
import com.stripe.payflow.api.dto.response.WalletBalanceResponse;
import com.stripe.payflow.api.dto.response.WalletResponse;
import com.stripe.payflow.application.service.WalletService;
import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.UserNotFoundException;
import com.stripe.payflow.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }

        if (walletRepository.existsByUserIdAndCurrency(request.userId(), request.currency())) {
            throw new IllegalArgumentException("User already has a wallet for this currency");
        }

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(request.userId());
        wallet.setCurrency(request.currency());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        Wallet savedWallet = walletRepository.save(wallet);
        return new WalletResponse(savedWallet.getId(), savedWallet.getUserId(), savedWallet.getCurrency(), savedWallet.getStatus().name(), savedWallet.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
        return new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getCurrency(), wallet.getStatus().name(), wallet.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
        return new WalletBalanceResponse(wallet.getId(), wallet.getCurrency(), wallet.getBalance(), wallet.getStatus().name());
    }
}
