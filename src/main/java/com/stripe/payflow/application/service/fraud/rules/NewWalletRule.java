package com.stripe.payflow.application.service.fraud.rules;

import com.stripe.payflow.application.service.fraud.FraudRule;
import com.stripe.payflow.domain.model.Wallet;
import com.stripe.payflow.domain.repository.WalletRepository;
import com.stripe.payflow.infrastructure.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NewWalletRule implements FraudRule {

    private final WalletRepository walletRepository;

    @Override
    public int calculateRiskScore(PaymentEvent event) {
        Optional<Wallet> walletOpt = walletRepository.findById(event.sourceWalletId());
        if (walletOpt.isEmpty()) {
            return 0; // Should not happen, but fail safe
        }
        
        Wallet wallet = walletOpt.get();
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        if (wallet.getCreatedAt() != null && wallet.getCreatedAt().isAfter(twentyFourHoursAgo)) {
            // New wallet. Is the transfer > 50% of the current balance?
            // (Using current balance as proxy for deposited balance for risk rule)
            BigDecimal halfBalance = wallet.getBalance().multiply(new BigDecimal("0.5"));
            
            if (event.amount().compareTo(halfBalance) > 0) {
                return 50;
            }
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "New Wallet Large Transfer";
    }
}
