package com.stripe.payflow.api.controller;

import com.stripe.payflow.domain.model.Notification;
import com.stripe.payflow.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID testWalletId;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testWalletId = UUID.randomUUID();
        
        testNotification = new Notification();
        testNotification.setId(UUID.randomUUID());
        testNotification.setWalletId(testWalletId);
        testNotification.setMessage("You received 100 USD.");
        testNotification.setRead(false);
        notificationRepository.save(testNotification);
    }

    @Test
    void getNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}/notifications", testWalletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].message").value("You received 100 USD."))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void markAsRead() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", testNotification.getId()))
                .andExpect(status().isNoContent());

        Notification updated = notificationRepository.findById(testNotification.getId()).orElseThrow();
        assertThat(updated.isRead()).isTrue();
    }
}
