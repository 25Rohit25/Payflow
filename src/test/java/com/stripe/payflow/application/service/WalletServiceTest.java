package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.CreateWalletRequest;
import com.stripe.payflow.api.dto.response.WalletBalanceResponse;
import com.stripe.payflow.api.dto.response.WalletResponse;
import com.stripe.payflow.application.service.impl.WalletServiceImpl;
import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.UserNotFoundException;
import com.stripe.payflow.exception.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWallet_Success() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(userId, "USD");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.existsByUserIdAndCurrency(userId, "USD")).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setCreatedAt(LocalDateTime.now());
            return w;
        });

        WalletResponse response = walletService.createWallet(request);

        assertNotNull(response);
        assertEquals("USD", response.currency());
        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(WalletStatus.ACTIVE, response.status());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void createWallet_UserNotFound() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(userId, "USD");

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> walletService.createWallet(request));
    }

    @Test
    void getBalance_Success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCurrency("EUR");
        wallet.setBalance(new BigDecimal("150.50"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletBalanceResponse response = walletService.getBalance(walletId);

        assertEquals("EUR", response.currency());
        assertEquals(new BigDecimal("150.50"), response.balance());
    }

    @Test
    void getWallet_NotFound() {
        UUID id = UUID.randomUUID();
        when(walletRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(id));
    }
}
