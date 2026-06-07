package com.stripe.payflow.infrastructure.outbox;

import com.stripe.payflow.domain.model.OutboxEvent;
import com.stripe.payflow.domain.repository.OutboxEventRepository;
import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events...", events.size());
        
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_EVENTS, event.getPayload()).get();
                outboxEventRepository.delete(event);
                log.debug("Successfully published outbox event {} to Kafka", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}. Will retry next cycle.", event.getId(), e);
                // Break to avoid out-of-order delivery if strict ordering is required, 
                // or just let it continue. We'll break here to maintain order.
                break;
            }
        }
    }
}
