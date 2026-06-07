package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.api.dto.response.TransferResponse;
import com.stripe.payflow.application.service.impl.TransferServiceImpl;
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
class TransferServiceTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;
    
    @Mock
    private LedgerEntryRepository entryRepository;
    
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Test
    void transfer_Success() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        
        TransferRequest request = new TransferRequest(sourceId, targetId, new BigDecimal("50.00"), "USD", "idem-t1");

        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(sourceId);
        sourceWallet.setCurrency("USD");
        sourceWallet.setBalance(new BigDecimal("100.00"));
        sourceWallet.setStatus(WalletStatus.ACTIVE);

        Wallet targetWallet = new Wallet();
        targetWallet.setId(targetId);
        targetWallet.setCurrency("USD");
        targetWallet.setBalance(new BigDecimal("20.00"));
        targetWallet.setStatus(WalletStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey("idem-t1")).thenReturn(Optional.empty());
        
        when(walletRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdForUpdate(targetId)).thenReturn(Optional.of(targetWallet));
        
        when(transactionRepository.save(any(LedgerTransaction.class))).thenAnswer(i -> {
            LedgerTransaction tx = i.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        TransferResponse response = transferService.transfer(request);

        assertNotNull(response);
        assertEquals("COMPLETED", response.status());
        assertEquals(new BigDecimal("50.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("70.00"), targetWallet.getBalance());
        
        verify(entryRepository, times(2)).save(any(LedgerEntry.class)); // Debit source, Credit target
    }

    @Test
    void transfer_InsufficientFunds() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        
        TransferRequest request = new TransferRequest(sourceId, targetId, new BigDecimal("150.00"), "USD", "idem-t2");

        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(sourceId);
        sourceWallet.setCurrency("USD");
        sourceWallet.setBalance(new BigDecimal("100.00"));
        sourceWallet.setStatus(WalletStatus.ACTIVE);

        Wallet targetWallet = new Wallet();
        targetWallet.setId(targetId);
        targetWallet.setCurrency("USD");
        targetWallet.setBalance(new BigDecimal("20.00"));
        targetWallet.setStatus(WalletStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey("idem-t2")).thenReturn(Optional.empty());
        
        when(walletRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdForUpdate(targetId)).thenReturn(Optional.of(targetWallet));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(request));
    }
}
