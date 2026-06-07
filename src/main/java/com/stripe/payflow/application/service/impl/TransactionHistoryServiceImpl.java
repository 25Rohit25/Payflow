package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.response.TransactionHistoryResponse;
import com.stripe.payflow.application.service.TransactionHistoryService;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final LedgerEntryRepository entryRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getWalletHistory(UUID walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException(walletId);
        }

        return entryRepository.findHistoryByWallet(walletId, startDate, endDate, pageable);
    }
}
