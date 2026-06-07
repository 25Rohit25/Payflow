package com.stripe.payflow.infrastructure.kafka.producer;

import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishEvent(PaymentEvent event) {
        log.info("Publishing PaymentEvent to Kafka: {}", event.eventId());
        
        CompletableFuture<SendResult<String, PaymentEvent>> future = 
                kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_EVENTS, event.transactionId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published PaymentEvent: {} to partition: {}", 
                        event.eventId(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish PaymentEvent: {}", event.eventId(), ex);
                // In a true Outbox pattern, we would mark an Outbox row as FAILED here.
            }
        });
    }
}
