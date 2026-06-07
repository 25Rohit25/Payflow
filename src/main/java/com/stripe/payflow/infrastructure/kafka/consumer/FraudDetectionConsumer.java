package com.stripe.payflow.infrastructure.kafka.consumer;

import com.stripe.payflow.application.service.fraud.FraudDetectionService;
import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = KafkaConfig.TOPIC_PAYMENT_EVENTS, groupId = "fraud-detection-group")
    public void consumePaymentEventForFraudChecks(@Payload PaymentEvent event) {
        log.info("Fraud engine received PaymentEvent: {}", event.transactionId());
        fraudDetectionService.inspectTransaction(event);
    }
}
