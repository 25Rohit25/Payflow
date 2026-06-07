package com.stripe.payflow.infrastructure.kafka;

import com.stripe.payflow.domain.model.TransactionStatus;
import com.stripe.payflow.domain.model.TransactionType;
import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import com.stripe.payflow.infrastructure.kafka.producer.PaymentEventProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class PaymentEventKafkaIntegrationTest {

    @Autowired
    private PaymentEventProducer producer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ConsumerFactory<String, PaymentEvent> consumerFactory;

    @Test
    void testKafkaEventPublishAndConsume() {
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                UUID.randomUUID(),
                UUID.randomUUID(),
                TransactionStatus.COMPLETED,
                LocalDateTime.now()
        );

        producer.publishEvent(event);

        Consumer<String, PaymentEvent> consumer = consumerFactory.createConsumer("testGroup", "client1");
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        ConsumerRecord<String, PaymentEvent> singleRecord = KafkaTestUtils.getSingleRecord(consumer, KafkaConfig.TOPIC_PAYMENT_EVENTS);
        
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.value().eventId()).isEqualTo(event.eventId());
        consumer.close();
    }
}
