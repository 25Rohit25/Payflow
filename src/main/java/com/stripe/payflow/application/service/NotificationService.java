package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    void createNotification(UUID walletId, String message);
    Page<NotificationResponse> getNotificationsForWallet(UUID walletId, Pageable pageable);
    void markAsRead(UUID notificationId);
}
