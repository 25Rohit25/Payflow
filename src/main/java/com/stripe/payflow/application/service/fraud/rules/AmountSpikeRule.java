package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class AmountSpikeRule implements FraudRule {

    private final LedgerEntryRepository ledgerEntryRepository;
    private static final BigDecimal MULTIPLIER = new BigDecimal("5");

    @Override
    public int calculateRiskScore(PaymentEvent event) {
        BigDecimal averageSpend = ledgerEntryRepository.getAverageDebitAmountByWalletId(event.sourceWalletId());
        
        if (averageSpend != null && averageSpend.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal spikeThreshold = averageSpend.multiply(MULTIPLIER);
            if (event.amount().compareTo(spikeThreshold) > 0) {
                return 40;
            }
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "Amount Spike (> 5x avg)";
    }
}
