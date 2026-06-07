package com.stripe.payflow.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import com.stripe.payflow.domain.model.OutboxEvent;
import com.stripe.payflow.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void saveEvent(PaymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("PAYMENT")
                    .aggregateId(UUID.randomUUID().toString()) // Random or actual aggregate ID
                    .type(event.type().name())
                    .payload(payload)
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Saved OutboxEvent for PaymentEvent: {}", event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for Outbox: {}", event, e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}
