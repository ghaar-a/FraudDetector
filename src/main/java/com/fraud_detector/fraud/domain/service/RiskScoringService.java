package com.fraud_detector.fraud.domain.service;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.transaction.domain.model.Transaction;

import java.util.List;
import java.util.Objects;

public class RiskScoringService {

    private final RiskScoringPolicy policy;

    public RiskScoringService(
            RiskScoringPolicy policy
    ) {
        this.policy = Objects.requireNonNull(
                policy,
                "Risk scoring policy cannot be null"
        );
    }

    public FraudAnalysis analyze(
            Transaction transaction,
            List<FraudReason> reasons
    ) {
        Objects.requireNonNull(
                transaction,
                "Transaction cannot be null"
        );

        Objects.requireNonNull(
                reasons,
                "Fraud reasons cannot be null"
        );

        RiskScore riskScore =
                policy.calculateRiskScore(
                        reasons
                );

        RiskLevel riskLevel =
                policy.determineRiskLevel(
                        riskScore
                );

        FraudDecision decision =
                policy.determineDecision(
                        riskLevel
                );

        return FraudAnalysis.create(
                transaction.id(),
                riskScore,
                riskLevel,
                decision,
                reasons
        );
    }
}