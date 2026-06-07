package com.stripe.payflow.infrastructure.kafka.event;

import com.stripe.payflow.domain.model.TransactionStatus;
import com.stripe.payflow.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentEvent(
    UUID eventId,
    UUID transactionId,
    TransactionType type,
    BigDecimal amount,
    String currency,
    UUID sourceWalletId,
    UUID targetWalletId,
    TransactionStatus status,
    LocalDateTime timestamp
) {}
