package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.WithdrawRequest;
import com.stripe.payflow.api.dto.response.WithdrawResponse;
import com.stripe.payflow.application.service.impl.WithdrawServiceImpl;
import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;
    
    @Mock
    private LedgerEntryRepository entryRepository;
    
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @Test
    void withdraw_Success() {
        UUID walletId = UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest(walletId, new BigDecimal("40.00"), "USD", "idem-w1");

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCurrency("USD");
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey("idem-w1")).thenReturn(Optional.empty());
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));
        
        when(transactionRepository.save(any(LedgerTransaction.class))).thenAnswer(i -> {
            LedgerTransaction tx = i.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        WithdrawResponse response = withdrawService.withdraw(request);

        assertNotNull(response);
        assertEquals("COMPLETED", response.status());
        assertEquals(new BigDecimal("60.00"), response.newBalance());
        
        verify(entryRepository, times(2)).save(any(LedgerEntry.class)); // Debit User, Credit System
    }

    @Test
    void withdraw_InsufficientFunds() {
        UUID walletId = UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest(walletId, new BigDecimal("150.00"), "USD", "idem-w2");

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCurrency("USD");
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey("idem-w2")).thenReturn(Optional.empty());
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () -> withdrawService.withdraw(request));
        verify(transactionRepository, never()).save(any());
        verify(entryRepository, never()).save(any());
    }
}
