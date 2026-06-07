package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighAmountRuleTest {

    private final HighAmountRule rule = new HighAmountRule();

    @Test
    void testIsFraudulent_HighAmount() {
        PaymentEvent event = new PaymentEvent(null, null, null, new BigDecimal("50000.01"), null, null, null, null, null);
        assertTrue(rule.isFraudulent(event));
    }

    @Test
    void testIsNotFraudulent_NormalAmount() {
        PaymentEvent event = new PaymentEvent(null, null, null, new BigDecimal("49999.99"), null, null, null, null, null);
        assertFalse(rule.isFraudulent(event));
    }
}
