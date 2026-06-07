package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {

    private final LedgerEntryRepository ledgerEntryRepository;
    private static final int MAX_TRANSACTIONS = 5;

    @Override
    public int calculateRiskScore(PaymentEvent event) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int count = ledgerEntryRepository.countByWalletIdAndCreatedAtAfter(event.sourceWalletId(), oneMinuteAgo);
        
        return count > MAX_TRANSACTIONS ? 30 : 0;
    }

    @Override
    public String getRuleName() {
        return "High Velocity (More than 5 in 1 min)";
    }
}
