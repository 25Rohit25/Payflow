package com.stripe.payflow.application.service.fraud;

import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;

public interface FraudDetectionService {
    void inspectTransaction(PaymentEvent event);
}
