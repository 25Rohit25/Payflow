package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.api.dto.response.DepositResponse;
import com.stripe.payflow.application.service.impl.DepositServiceImpl;
import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
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
class DepositServiceTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;
    
    @Mock
    private LedgerEntryRepository entryRepository;
    
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private DepositServiceImpl depositService;

    @Test
    void deposit_Success() {
        UUID walletId = UUID.randomUUID();
        DepositRequest request = new DepositRequest(walletId, new BigDecimal("100.00"), "USD", "idem-123");

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCurrency("USD");
        wallet.setBalance(new BigDecimal("50.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.empty());
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));
        
        when(transactionRepository.save(any(LedgerTransaction.class))).thenAnswer(i -> {
            LedgerTransaction tx = i.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        DepositResponse response = depositService.deposit(request);

        assertNotNull(response);
        assertEquals("COMPLETED", response.status());
        assertEquals(new BigDecimal("150.00"), response.newBalance());
        
        verify(transactionRepository, times(1)).save(any(LedgerTransaction.class));
        verify(entryRepository, times(2)).save(any(LedgerEntry.class)); // Debit and Credit
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void deposit_IdempotencyKeyUsed() {
        DepositRequest request = new DepositRequest(UUID.randomUUID(), new BigDecimal("100.00"), "USD", "idem-123");
        when(transactionRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.of(new LedgerTransaction()));

        assertThrows(IllegalArgumentException.class, () -> depositService.deposit(request));
    }
}
