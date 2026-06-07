package com.stripe.payflow.domain.repository;

import com.stripe.payflow.domain.model.FraudLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FraudLogRepository extends JpaRepository<FraudLog, UUID> {
}
