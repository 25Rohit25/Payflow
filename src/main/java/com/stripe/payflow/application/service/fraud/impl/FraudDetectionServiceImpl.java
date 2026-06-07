package com.stripe.payflow.application.service.fraud.impl;

import com.stripe.payflow.application.service.fraud.FraudDetectionService;
import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.model.FraudLog;
import com.stripe.payflow.domain.model.WalletStatus;
import com.stripe.payflow.domain.repository.FraudLogRepository;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.infrastructure.kafka.config.KafkaConfig;
import com.stripe.payflow.infrastructure.kafka.event.FraudAlertEvent;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final List<FraudRule> rules;
    private final FraudLogRepository fraudLogRepository;
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public void inspectTransaction(PaymentEvent event) {
        UUID targetWalletToCheck = event.sourceWalletId();
        
        int totalRiskScore = 0;
        StringBuilder reasons = new StringBuilder();

        for (FraudRule rule : rules) {
            int score = rule.calculateRiskScore(event);
            if (score > 0) {
                totalRiskScore += score;
                reasons.append(rule.getRuleName()).append(" (+").append(score).append("), ");
            }
        }

        if (totalRiskScore >= 70) {
            String combinedReason = "Risk Score " + totalRiskScore + ": " + reasons.toString();
            log.warn("FRAUD DETECTED: Transaction {} flagged with {}", event.transactionId(), combinedReason);
            triggerFraudAction(targetWalletToCheck, event.transactionId(), combinedReason);
        }
    }

    private void triggerFraudAction(UUID walletId, UUID transactionId, String reason) {
        // 1. Log to DB
        FraudLog fraudLog = new FraudLog();
        fraudLog.setId(UUID.randomUUID());
        fraudLog.setWalletId(walletId);
        fraudLog.setTransactionId(transactionId);
        fraudLog.setReason(reason);
        fraudLogRepository.save(fraudLog);

        // 2. Block the wallet
        walletRepository.findById(walletId).ifPresent(wallet -> {
            wallet.setStatus(WalletStatus.BLOCKED);
            walletRepository.save(wallet);
            log.warn("Wallet {} has been BLOCKED due to fraud.", walletId);
        });

        // 3. Publish SecOps Alert
        FraudAlertEvent alertEvent = new FraudAlertEvent(
                UUID.randomUUID(),
                transactionId,
                walletId,
                reason,
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_FRAUD_ALERTS, walletId.toString(), alertEvent);
        log.info("Published FraudAlertEvent to topic '{}'", KafkaConfig.TOPIC_FRAUD_ALERTS);
    }
}
