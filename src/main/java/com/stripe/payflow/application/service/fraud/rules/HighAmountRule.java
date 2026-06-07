package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("50000.00");

    @Override
    public boolean isFraudulent(PaymentEvent event) {
        return event.amount().compareTo(THRESHOLD) > 0;
    }

    @Override
    public String getRuleName() {
        return "Single Transaction Amount > 50000";
    }
}
