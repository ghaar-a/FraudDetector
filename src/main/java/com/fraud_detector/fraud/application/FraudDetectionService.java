package com.fraud_detector.fraud.application;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.fraud.domain.rule.FraudRuleEngine;
import com.fraud_detector.fraud.domain.service.RiskScoringService;
import com.fraud_detector.transaction.domain.model.Transaction;

import java.util.List;
import java.util.Objects;

public class FraudDetectionService {

    private final FraudRuleEngine fraudRuleEngine;
    private final RiskScoringService riskScoringService;

    public FraudDetectionService(
            FraudRuleEngine fraudRuleEngine,
            RiskScoringService riskScoringService
    ) {
        this.fraudRuleEngine = Objects.requireNonNull(
                fraudRuleEngine,
                "Fraud rule engine cannot be null"
        );

        this.riskScoringService = Objects.requireNonNull(
                riskScoringService,
                "Risk scoring service cannot be null"
        );
    }

    public FraudAnalysis analyze(
            Transaction transaction,
            FraudRuleContext context
    ) {
        Objects.requireNonNull(
                transaction,
                "Transaction cannot be null"
        );

        Objects.requireNonNull(
                context,
                "Fraud rule context cannot be null"
        );

        List<FraudReason> fraudReasons =
                fraudRuleEngine.evaluate(
                        transaction,
                        context
                );

        return riskScoringService.analyze(
                transaction,
                fraudReasons
        );
    }
}