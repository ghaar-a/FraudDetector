package com.fraud_detector.fraud.domain.service;

import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class RiskScoringPolicy {

    private final Map<FraudReason, BigDecimal> reasonWeights;

    public RiskScoringPolicy(
            Map<FraudReason, BigDecimal> reasonWeights
    ) {
        Objects.requireNonNull(
                reasonWeights,
                "Reason weights cannot be null"
        );

        if (reasonWeights.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one reason weight is required"
            );
        }

        reasonWeights.forEach(
                (reason, weight) -> {
                    Objects.requireNonNull(
                            reason,
                            "Fraud reason cannot be null"
                    );

                    Objects.requireNonNull(
                            weight,
                            "Reason weight cannot be null"
                    );

                    if (weight.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                "Reason weight cannot be negative"
                        );
                    }
                }
        );

        this.reasonWeights = Map.copyOf(
                reasonWeights
        );
    }

    public RiskScore calculateRiskScore(
            Iterable<FraudReason> reasons
    ) {
        Objects.requireNonNull(
                reasons,
                "Fraud reasons cannot be null"
        );

        BigDecimal totalScore =
                java.math.BigDecimal.ZERO;

        for (FraudReason reason : reasons) {
            BigDecimal weight =
                    reasonWeights.getOrDefault(
                            reason,
                            BigDecimal.ZERO
                    );

            totalScore =
                    totalScore.add(weight);
        }

        return RiskScore.of(
                totalScore.min(BigDecimal.ONE)
        );
    }

    public RiskLevel determineRiskLevel(
            RiskScore riskScore
    ) {
        Objects.requireNonNull(
                riskScore,
                "Risk score cannot be null"
        );

        if (riskScore.isCritical()) {
            return RiskLevel.CRITICAL;
        }

        if (riskScore.isHigh()) {
            return RiskLevel.HIGH;
        }

        if (riskScore.isMedium()) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    public FraudDecision determineDecision(
            RiskLevel riskLevel
    ) {
        Objects.requireNonNull(
                riskLevel,
                "Risk level cannot be null"
        );

        return switch (riskLevel) {
            case LOW -> FraudDecision.APPROVED;
            case MEDIUM, HIGH -> FraudDecision.REVIEW;
            case CRITICAL -> FraudDecision.BLOCKED;
        };
    }
}