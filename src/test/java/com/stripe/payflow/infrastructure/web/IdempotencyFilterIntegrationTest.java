package com.stripe.payflow.infrastructure.web;

import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.domain.model.IdempotencyRecord;
import com.stripe.payflow.domain.model.IdempotencyStatus;
import com.stripe.payflow.domain.model.User;
import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.IdempotencyRecordRepository;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IdempotencyFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdempotencyRecordRepository idempotencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("idem-test@test.com");
        user.setFirstName("Idem");
        user.setLastName("Test");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        testWallet = new Wallet();
        testWallet.setId(UUID.randomUUID());
        testWallet.setUserId(user.getId());
        testWallet.setCurrency("USD");
        testWallet.setBalance(BigDecimal.ZERO);
        testWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(testWallet);
    }

    @Test
    void testIdempotency_ReturnsCachedResponseOnRetry() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        DepositRequest request = new DepositRequest(testWallet.getId(), new BigDecimal("50.00"), "USD");
        String payload = objectMapper.writeValueAsString(request);

        // 1. First request - should process normally
        MvcResult firstResult = mockMvc.perform(post("/api/v1/deposits")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String firstResponse = firstResult.getResponse().getContentAsString();

        // Verify it was stored
        IdempotencyRecord record = idempotencyRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        assertThat(record.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(record.getResponsePayload()).isEqualTo(firstResponse);

        // 2. Second request with same key - should return cached response
        MvcResult secondResult = mockMvc.perform(post("/api/v1/deposits")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String secondResponse = secondResult.getResponse().getContentAsString();

        // 3. Responses should be identical
        assertThat(secondResponse).isEqualTo(firstResponse);
        
        // 4. Verify balance is only 50 (not 100) indicating the service logic only ran once
        Wallet updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance().compareTo(new BigDecimal("50.00"))).isEqualTo(0);
    }
}
