package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.request.WithdrawRequest;
import com.stripe.payflow.api.dto.response.WithdrawResponse;
import com.stripe.payflow.application.service.WithdrawService;
import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.InsufficientFundsException;
import com.stripe.payflow.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request, String idempotencyKey) {
        // 2. Lock Wallet for Update (Pessimistic Locking)
        Wallet sourceWallet = walletRepository.findByIdForUpdate(request.sourceWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.sourceWalletId()));

        if (!sourceWallet.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        if (sourceWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalArgumentException("Wallet is not active");
        }

        // 3. Balance Check (Crucial for Withdraw)
        if (sourceWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(sourceWallet.getId());
        }

        // 4. Create Ledger Transaction
        LedgerTransaction tx = new LedgerTransaction();
        tx.setId(UUID.randomUUID());
        tx.setIdempotencyKey(idempotencyKey);
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx = transactionRepository.save(tx);

        // 5. Create Ledger Entries (Double Entry)
        // Withdrawal means we Debit the User Wallet and Credit the System Liability/Asset.
        LedgerEntry userDebit = new LedgerEntry();
        userDebit.setId(UUID.randomUUID());
        userDebit.setTransactionId(tx.getId());
        userDebit.setWalletId(sourceWallet.getId());
        userDebit.setAmount(request.amount());
        userDebit.setDirection(EntryDirection.DEBIT);
        entryRepository.save(userDebit);

        LedgerEntry systemCredit = new LedgerEntry();
        systemCredit.setId(UUID.randomUUID());
        systemCredit.setTransactionId(tx.getId());
        systemCredit.setWalletId(null); 
        systemCredit.setAmount(request.amount());
        systemCredit.setDirection(EntryDirection.CREDIT);
        entryRepository.save(systemCredit);

        // 6. Update Wallet Balance
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        walletRepository.save(sourceWallet);

        return new WithdrawResponse(
                tx.getId(),
                tx.getStatus().name(),
                sourceWallet.getBalance()
        );
    }
}
