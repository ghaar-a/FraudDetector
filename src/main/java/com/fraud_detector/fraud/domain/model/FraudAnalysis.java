package com.fraud_detector.fraud.domain.model;

import com.fraud_detector.transaction.domain.model.TransactionId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record FraudAnalysis(
        UUID id,
        TransactionId transactionId,
        RiskScore riskScore,
        RiskLevel riskLevel,
        FraudDecision decision,
        List<FraudReason> reasons,
        Instant analyzedAt
) {

    public FraudAnalysis {
        Objects.requireNonNull(id, "Analysis ID cannot be null");
        Objects.requireNonNull(
                transactionId,
                "Transaction ID cannot be null"
        );
        Objects.requireNonNull(
                riskScore,
                "Risk score cannot be null"
        );
        Objects.requireNonNull(
                riskLevel,
                "Risk level cannot be null"
        );
        Objects.requireNonNull(
                decision,
                "Fraud decision cannot be null"
        );
        Objects.requireNonNull(
                reasons,
                "Fraud reasons cannot be null"
        );
        Objects.requireNonNull(
                analyzedAt,
                "Analysis timestamp cannot be null"
        );

        reasons = List.copyOf(reasons);
    }

    public static FraudAnalysis create(
            TransactionId transactionId,
            RiskScore riskScore,
            RiskLevel riskLevel,
            FraudDecision decision,
            List<FraudReason> reasons
    ) {
        return new FraudAnalysis(
                UUID.randomUUID(),
                transactionId,
                riskScore,
                riskLevel,
                decision,
                reasons,
                Instant.now()
        );
    }

    public boolean isFraudulent() {
        return decision == FraudDecision.BLOCKED;
    }

    public boolean requiresManualReview() {
        return decision == FraudDecision.REVIEW;
    }
}