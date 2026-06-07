package com.stripe.payflow.domain.repository;

import com.stripe.payflow.domain.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stripe.payflow.api.dto.response.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    
    @Query("SELECT new com.stripe.payflow.api.dto.response.TransactionHistoryResponse(" +
           "t.id, t.type, e.amount, e.direction, t.status, e.createdAt) " +
           "FROM LedgerEntry e JOIN LedgerTransaction t ON e.transactionId = t.id " +
           "WHERE e.walletId = :walletId " +
           "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR e.createdAt <= :endDate)")
    Page<TransactionHistoryResponse> findHistoryByWallet(
        @Param("walletId") UUID walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    int countByWalletIdAndCreatedAtAfter(UUID walletId, LocalDateTime timestamp);

    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.walletId = :walletId AND l.direction = 'DEBIT' AND l.createdAt >= :since")
    BigDecimal sumDebitAmountByWalletIdAndCreatedAtAfter(@Param("walletId") UUID walletId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(l.amount) FROM LedgerEntry l WHERE l.walletId = :walletId AND l.direction = 'DEBIT'")
    BigDecimal getAverageDebitAmountByWalletId(@Param("walletId") UUID walletId);
}
