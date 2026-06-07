package com.stripe.payflow.application.service.fraud;

import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;

public interface FraudRule {
    /**
     * @param event The payment event to analyze
     * @return Risk score (0 to 100) added by this rule
     */
    int calculateRiskScore(PaymentEvent event);
    
    String getRuleName();
}
