package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.domain.repository.LedgerEntryRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HighVelocityRuleTest {

    @Mock
    private LedgerEntryRepository repository;

    @InjectMocks
    private HighVelocityRule rule;

    @Test
    void testIsFraudulent_HighVelocity() {
        UUID walletId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(null, null, null, null, null, walletId, null, null, null);
        
        when(repository.countByWalletIdAndCreatedAtAfter(eq(walletId), any())).thenReturn(6);
        
        assertTrue(rule.isFraudulent(event));
    }

    @Test
    void testIsNotFraudulent_NormalVelocity() {
        UUID walletId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(null, null, null, null, null, walletId, null, null, null);
        
        when(repository.countByWalletIdAndCreatedAtAfter(eq(walletId), any())).thenReturn(5);
        
        assertFalse(rule.isFraudulent(event));
    }
}
