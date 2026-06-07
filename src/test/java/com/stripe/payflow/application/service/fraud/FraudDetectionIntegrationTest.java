package com.stripe.payflow.application.service.fraud;

import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.FraudLogRepository;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.FraudAlertEvent;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
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
class FraudDetectionIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private FraudLogRepository fraudLogRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ConsumerFactory<String, FraudAlertEvent> consumerFactory;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet();
        testWallet.setId(UUID.randomUUID());
        testWallet.setUserId(UUID.randomUUID());
        testWallet.setCurrency("USD");
        testWallet.setBalance(new BigDecimal("1000000.00"));
        testWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(testWallet);
    }

    @Test
    void testFraudDetection_BlocksWalletAndPublishesAlert() {
        // Trigger HighAmountRule
        PaymentEvent fraudEvent = new PaymentEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("50001.00"),
                "USD",
                testWallet.getId(),
                UUID.randomUUID(),
                null,
                LocalDateTime.now()
        );

        // Publish to payment-events (acting as the core engine)
        kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_EVENTS, fraudEvent.transactionId().toString(), fraudEvent);

        // Wait for FraudDetectionConsumer and Service to run
        Consumer<String, FraudAlertEvent> alertConsumer = consumerFactory.createConsumer("testAlertGroup", "client1");
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(alertConsumer);

        // Verify FraudAlertEvent was published to fraud-alerts
        ConsumerRecord<String, FraudAlertEvent> alertRecord = KafkaTestUtils.getSingleRecord(alertConsumer, KafkaConfig.TOPIC_FRAUD_ALERTS);
        assertThat(alertRecord).isNotNull();
        assertThat(alertRecord.value().walletId()).isEqualTo(testWallet.getId());
        assertThat(alertRecord.value().reason()).isEqualTo("Single Transaction Amount > 50000");

        // Verify wallet is blocked
        Wallet blockedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(blockedWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);

        // Verify fraud log
        assertThat(fraudLogRepository.findAll()).hasSize(1);
        
        alertConsumer.close();
    }
}
