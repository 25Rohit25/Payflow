package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.response.NotificationResponse;
import com.stripe.payflow.application.service.NotificationService;
import com.stripe.payflow.domain.model.Notification;
import com.stripe.payflow.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(UUID walletId, String message) {
        if (walletId == null) return;
        
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setWalletId(walletId);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForWallet(UUID walletId, Pageable pageable) {
        return notificationRepository.findByWalletId(walletId, pageable)
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getWalletId(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()
                ));
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
