package com.stripe.payflow.api.controller;

import com.stripe.payflow.domain.model.*;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.domain.repository.LedgerTransactionRepository;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionHistoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerTransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository entryRepository;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("history-test@test.com");
        user.setFirstName("History");
        user.setLastName("User");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        testWallet = new Wallet();
        testWallet.setId(UUID.randomUUID());
        testWallet.setUserId(user.getId());
        testWallet.setCurrency("USD");
        testWallet.setBalance(new BigDecimal("100.00"));
        testWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(testWallet);

        // Seed some history
        LedgerTransaction tx = new LedgerTransaction();
        tx.setId(UUID.randomUUID());
        tx.setIdempotencyKey("idem-h1");
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setTransactionId(tx.getId());
        entry.setWalletId(testWallet.getId());
        entry.setAmount(new BigDecimal("100.00"));
        entry.setDirection(EntryDirection.CREDIT);
        entryRepository.save(entry);
    }

    @Test
    void testGetTransactions_Success() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}/transactions", testWallet.getId())
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.content[0].direction").value("CREDIT"))
                .andExpect(jsonPath("$.content[0].amount").value(100.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
