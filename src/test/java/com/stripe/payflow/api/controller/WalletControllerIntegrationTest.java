package com.stripe.payflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.payflow.api.dto.request.CreateWalletRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("wallet-test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setStatus("ACTIVE");
        userRepository.save(testUser);
    }

    @Test
    void testCreateWallet() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest(testUser.getId(), "USD");

        mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetBalance() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(testUser.getId());
        wallet.setCurrency("EUR");
        wallet.setBalance(new BigDecimal("250.75"));
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);

        mockMvc.perform(get("/api/v1/wallets/{id}/balance", wallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250.75))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }
}
