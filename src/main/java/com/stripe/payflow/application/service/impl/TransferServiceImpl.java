package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.api.dto.response.TransferResponse;
import com.stripe.payflow.application.service.TransferService;
import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.InsufficientFundsException;
import com.stripe.payflow.exception.WalletNotFoundException;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import com.stripe.payflow.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final WalletRepository walletRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey) {
        if (request.sourceWalletId().equals(request.targetWalletId())) {
            throw new IllegalArgumentException("Source and Target wallets cannot be the same");
        }

        // 2. Deadlock Prevention: Always lock wallets in a deterministic order
        UUID firstLockId = request.sourceWalletId().compareTo(request.targetWalletId()) < 0 
            ? request.sourceWalletId() : request.targetWalletId();
        UUID secondLockId = request.sourceWalletId().compareTo(request.targetWalletId()) < 0 
            ? request.targetWalletId() : request.sourceWalletId();

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstLockId)
            .orElseThrow(() -> new WalletNotFoundException(firstLockId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondLockId)
            .orElseThrow(() -> new WalletNotFoundException(secondLockId));

        Wallet sourceWallet = firstWallet.getId().equals(request.sourceWalletId()) ? firstWallet : secondWallet;
        Wallet targetWallet = secondWallet.getId().equals(request.targetWalletId()) ? secondWallet : firstWallet;

        // 3. Validation
        if (!sourceWallet.getCurrency().equals(request.currency()) || !targetWallet.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch between transfer request and wallets");
        }

        if (sourceWallet.getStatus() != WalletStatus.ACTIVE || targetWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalArgumentException("One or both wallets are not active");
        }

        if (sourceWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(sourceWallet.getId());
        }

        // 4. Create Ledger Transaction
        LedgerTransaction tx = new LedgerTransaction();
        tx.setId(UUID.randomUUID());
        tx.setIdempotencyKey(idempotencyKey);
        tx.setType(TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx = transactionRepository.save(tx);

        // 5. Create Ledger Entries (Double Entry: Debit Source, Credit Target)
        LedgerEntry sourceDebit = new LedgerEntry();
        sourceDebit.setId(UUID.randomUUID());
        sourceDebit.setTransactionId(tx.getId());
        sourceDebit.setWalletId(sourceWallet.getId());
        sourceDebit.setAmount(request.amount());
        sourceDebit.setDirection(EntryDirection.DEBIT);
        entryRepository.save(sourceDebit);

        LedgerEntry targetCredit = new LedgerEntry();
        targetCredit.setId(UUID.randomUUID());
        targetCredit.setTransactionId(tx.getId());
        targetCredit.setWalletId(targetWallet.getId());
        targetCredit.setAmount(request.amount());
        targetCredit.setDirection(EntryDirection.CREDIT);
        entryRepository.save(targetCredit);

        // 6. Update Wallet Balances
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        targetWallet.setBalance(targetWallet.getBalance().add(request.amount()));
        
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // 8. Publish Event internally (will be caught AFTER_COMMIT)
        PaymentEvent paymentEvent = new PaymentEvent(
                UUID.randomUUID(),
                tx.getId(),
                TransactionType.TRANSFER,
                request.amount(),
                request.currency(),
                sourceWallet.getId(),
                targetWallet.getId(),
                tx.getStatus(),
                LocalDateTime.now()
        );
        outboxService.saveEvent(paymentEvent);

        return new TransferResponse(
                tx.getId(),
                tx.getStatus().name(),
                sourceWallet.getBalance(),
                targetWallet.getBalance()
        );
    }
}
