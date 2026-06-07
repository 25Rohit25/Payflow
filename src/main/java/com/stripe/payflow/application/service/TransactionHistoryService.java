package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.response.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionHistoryService {
    Page<TransactionHistoryResponse> getWalletHistory(UUID walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
