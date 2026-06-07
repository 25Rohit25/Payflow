package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.response.TransactionHistoryResponse;
import com.stripe.payflow.application.service.impl.TransactionHistoryServiceImpl;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock
    private LedgerEntryRepository entryRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionHistoryServiceImpl historyService;

    @Test
    void getWalletHistory_Success() {
        UUID walletId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        when(walletRepository.existsById(walletId)).thenReturn(true);
        when(entryRepository.findHistoryByWallet(eq(walletId), any(), any(), eq(pageRequest)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<TransactionHistoryResponse> result = historyService.getWalletHistory(walletId, null, null, pageRequest);

        assertNotNull(result);
        verify(walletRepository).existsById(walletId);
        verify(entryRepository).findHistoryByWallet(walletId, null, null, pageRequest);
    }

    @Test
    void getWalletHistory_WalletNotFound() {
        UUID walletId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        when(walletRepository.existsById(walletId)).thenReturn(false);

        assertThrows(WalletNotFoundException.class, 
                () -> historyService.getWalletHistory(walletId, null, null, pageRequest));
        verify(entryRepository, never()).findHistoryByWallet(any(), any(), any(), any());
    }
}
