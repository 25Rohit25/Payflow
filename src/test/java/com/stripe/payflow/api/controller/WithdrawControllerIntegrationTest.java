package com.stripe.payflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.payflow.api.dto.request.WithdrawRequest;
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
class WithdrawControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("withdraw-test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        testWallet = new Wallet();
        testWallet.setId(UUID.randomUUID());
        testWallet.setUserId(user.getId());
        testWallet.setCurrency("USD");
        testWallet.setBalance(new BigDecimal("500.00"));
        testWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(testWallet);
    }

    @Test
    void testWithdraw_Success() throws Exception {
        WithdrawRequest request = new WithdrawRequest(testWallet.getId(), new BigDecimal("150.00"), "USD", "idem-wtx-1");

        mockMvc.perform(post("/api/v1/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.newBalance").value(350.00));
    }

    @Test
    void testWithdraw_InsufficientFunds() throws Exception {
        WithdrawRequest request = new WithdrawRequest(testWallet.getId(), new BigDecimal("1000.00"), "USD", "idem-wtx-2");

        mockMvc.perform(post("/api/v1/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()); // Expecting 422
    }
}
