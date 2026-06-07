package com.stripe.payflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.domain.model.User;
import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private Wallet sourceWallet;
    private Wallet targetWallet;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("transfer-source@test.com");
        user1.setFirstName("Source");
        user1.setLastName("User");
        user1.setStatus("ACTIVE");
        userRepository.save(user1);

        sourceWallet = new Wallet();
        sourceWallet.setId(UUID.randomUUID());
        sourceWallet.setUserId(user1.getId());
        sourceWallet.setCurrency("USD");
        sourceWallet.setBalance(new BigDecimal("500.00"));
        sourceWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(sourceWallet);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("transfer-target@test.com");
        user2.setFirstName("Target");
        user2.setLastName("User");
        user2.setStatus("ACTIVE");
        userRepository.save(user2);

        targetWallet = new Wallet();
        targetWallet.setId(UUID.randomUUID());
        targetWallet.setUserId(user2.getId());
        targetWallet.setCurrency("USD");
        targetWallet.setBalance(new BigDecimal("0.00"));
        targetWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(targetWallet);
    }

    @Test
    void testTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest(sourceWallet.getId(), targetWallet.getId(), new BigDecimal("150.00"), "USD", "idem-txf-1");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testTransfer_InsufficientFunds() throws Exception {
        TransferRequest request = new TransferRequest(sourceWallet.getId(), targetWallet.getId(), new BigDecimal("1000.00"), "USD", "idem-txf-2");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()); 
    }
}
