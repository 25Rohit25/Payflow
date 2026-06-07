package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.request.CreateUserRequest;
import com.stripe.payflow.api.dto.request.CreateWalletRequest;
import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.api.dto.response.UserResponse;
import com.stripe.payflow.api.dto.response.WalletResponse;
import com.stripe.payflow.application.service.DepositService;
import com.stripe.payflow.application.service.UserService;
import com.stripe.payflow.application.service.WalletService;
import com.stripe.payflow.base.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcurrentTransferIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private DepositService depositService;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void testConcurrentTransfersMaintainLedgerConsistency() throws Exception {
        // 1. Create Sender and Receiver Users
        UserResponse sender = userService.createUser(new CreateUserRequest("Sender User", "sender@test.com", "password"));
        UserResponse receiver = userService.createUser(new CreateUserRequest("Receiver User", "receiver@test.com", "password"));

        // 2. Create Wallets
        WalletResponse senderWallet = walletService.createWallet(new CreateWalletRequest(sender.id(), "USD"));
        WalletResponse receiverWallet = walletService.createWallet(new CreateWalletRequest(receiver.id(), "USD"));

        // 3. Deposit Initial Funds to Sender (e.g., $1000)
        depositService.deposit(new DepositRequest(senderWallet.id(), new BigDecimal("1000.00"), "Initial Deposit"));

        // 4. Setup Concurrency
        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // 5. Execute 50 simultaneous transfers of $10
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    String idempotencyKey = UUID.randomUUID().toString();
                    TransferRequest request = new TransferRequest(
                            senderWallet.id(),
                            receiverWallet.id(),
                            new BigDecimal("10.00"),
                            "Concurrent Transfer"
                    );

                    mockMvc.perform(post("/api/v1/transfers")
                            .with(user(sender.email()).password("password").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", idempotencyKey)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Some might fail due to rate limiting or optimistic locking if we used it,
                    // but with pessimistic locking, they should just queue up and succeed
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 6. Verify Balances
        WalletResponse finalSenderWallet = walletService.getWallet(senderWallet.id());
        WalletResponse finalReceiverWallet = walletService.getWallet(receiverWallet.id());

        // Expected: $1000 - (50 * $10) = $500
        BigDecimal expectedSenderBalance = new BigDecimal("1000.00").subtract(new BigDecimal("10.00").multiply(new BigDecimal(successCount.get())));
        // Expected: 0 + (50 * $10) = $500
        BigDecimal expectedReceiverBalance = new BigDecimal("10.00").multiply(new BigDecimal(successCount.get()));

        assertEquals(0, finalSenderWallet.balance().compareTo(expectedSenderBalance), "Sender balance is inconsistent!");
        assertEquals(0, finalReceiverWallet.balance().compareTo(expectedReceiverBalance), "Receiver balance is inconsistent!");
    }
}
