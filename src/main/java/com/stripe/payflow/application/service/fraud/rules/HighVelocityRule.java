package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HighVelocityRule implements FraudRule {

    private final LedgerEntryRepository ledgerEntryRepository;
    private static final int MAX_TRANSACTIONS = 5;

    @Override
    public boolean isFraudulent(PaymentEvent event) {
        // "More than 5 transactions in 1 minute"
        // We look specifically at the source wallet acting as the sender (DEBIT)
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int count = ledgerEntryRepository.countByWalletIdAndCreatedAtAfter(event.sourceWalletId(), oneMinuteAgo);
        
        return count > MAX_TRANSACTIONS;
    }

    @Override
    public String getRuleName() {
        return "More than 5 transactions in 1 minute";
    }
}
