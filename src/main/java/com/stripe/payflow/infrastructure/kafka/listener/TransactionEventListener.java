package com.stripe.payflow.infrastructure.kafka.listener;

import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import com.stripe.payflow.infrastructure.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventListener {

    private final PaymentEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Transaction committed successfully. Forwarding event {} to Kafka.", event.eventId());
        producer.publishEvent(event);
    }
}
