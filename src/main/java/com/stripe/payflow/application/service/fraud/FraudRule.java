package com.stripe.payflow.application.service.fraud;

import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;

public interface FraudRule {
    /**
     * @return true if the event violates this rule.
     */
    boolean isFraudulent(PaymentEvent event);

    /**
     * @return The descriptive name/reason of the rule.
     */
    String getRuleName();
}
