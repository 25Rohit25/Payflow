package com.stripe.payflow.infrastructure.kafka.consumer;

import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import com.stripe.payflow.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaConfig.TOPIC_PAYMENT_EVENTS, groupId = "notification-service-group")
    public void consumePaymentEvent(@Payload PaymentEvent event) {
        log.info("Received PaymentEvent in Notification Consumer: {}", event.eventId());
        
        switch (event.type()) {
            case TRANSFER:
                notificationService.createNotification(event.sourceWalletId(), 
                        String.format("You sent %s %s.", event.amount(), event.currency()));
                notificationService.createNotification(event.targetWalletId(), 
                        String.format("You received %s %s.", event.amount(), event.currency()));
                break;
            case DEPOSIT:
                notificationService.createNotification(event.targetWalletId(), 
                        String.format("Deposit of %s %s successful.", event.amount(), event.currency()));
                break;
            case WITHDRAWAL:
                notificationService.createNotification(event.sourceWalletId(), 
                        String.format("Withdrawal of %s %s successful.", event.amount(), event.currency()));
                break;
        }
    }
}
