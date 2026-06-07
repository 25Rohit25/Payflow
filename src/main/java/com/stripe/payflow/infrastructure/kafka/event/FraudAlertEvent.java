package com.stripe.payflow.infrastructure.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudAlertEvent(
    UUID alertId,
    UUID transactionId,
    UUID walletId,
    String reason,
    LocalDateTime timestamp
) {}
