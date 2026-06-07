package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.api.dto.response.DepositResponse;
import com.stripe.payflow.application.service.DepositService;
import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public DepositResponse deposit(DepositRequest request, String idempotencyKey) {
        // 2. Lock Wallet for Update (Pessimistic Locking)
        Wallet targetWallet = walletRepository.findByIdForUpdate(request.targetWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.targetWalletId()));

        if (!targetWallet.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        if (targetWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalArgumentException("Wallet is not active");
        }

        // 3. Create Ledger Transaction
        LedgerTransaction tx = new LedgerTransaction();
        tx.setId(UUID.randomUUID());
        tx.setIdempotencyKey(idempotencyKey);
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx = transactionRepository.save(tx);

        // 4. Create Ledger Entries (Double Entry)
        // System Bank Account Liability (Credit or Debit? In traditional systems, depositing money means we owe the user, so we credit their account and debit the system cash asset).
        // For simplicity, Debit System (NULL wallet), Credit User Wallet.
        
        LedgerEntry systemDebit = new LedgerEntry();
        systemDebit.setId(UUID.randomUUID());
        systemDebit.setTransactionId(tx.getId());
        systemDebit.setWalletId(null); 
        systemDebit.setAmount(request.amount());
        systemDebit.setDirection(EntryDirection.DEBIT);
        entryRepository.save(systemDebit);

        LedgerEntry userCredit = new LedgerEntry();
        userCredit.setId(UUID.randomUUID());
        userCredit.setTransactionId(tx.getId());
        userCredit.setWalletId(targetWallet.getId());
        userCredit.setAmount(request.amount());
        userCredit.setDirection(EntryDirection.CREDIT);
        entryRepository.save(userCredit);

        // 5. Update Wallet Balance
        targetWallet.setBalance(targetWallet.getBalance().add(request.amount()));
        walletRepository.save(targetWallet);

        return new DepositResponse(
                tx.getId(),
                tx.getStatus().name(),
                targetWallet.getBalance()
        );
    }
}
