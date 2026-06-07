package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HighDailySpendRule implements FraudRule {

    private final LedgerEntryRepository ledgerEntryRepository;
    private static final BigDecimal MAX_DAILY_SPEND = new BigDecimal("100000.00");

    @Override
    public boolean isFraudulent(PaymentEvent event) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        
        // Sum all DEBITs (money leaving the wallet) today
        BigDecimal totalSpentToday = ledgerEntryRepository.sumDebitAmountByWalletIdAndCreatedAtAfter(event.sourceWalletId(), startOfDay);
        
        return totalSpentToday.compareTo(MAX_DAILY_SPEND) > 0;
    }

    @Override
    public String getRuleName() {
        return "Daily spend > 100000";
    }
}
