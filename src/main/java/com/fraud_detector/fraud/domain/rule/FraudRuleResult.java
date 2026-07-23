package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;

import java.util.Objects;

public record FraudRuleResult(
        FraudReason reason,
        boolean suspicious
) {

    public FraudRuleResult {
        Objects.requireNonNull(
                reason,
                "Fraud reason cannot be null"
        );
    }

    public static FraudRuleResult suspicious(
            FraudReason reason
    ) {
        return new FraudRuleResult(
                reason,
                true
        );
    }

    public static FraudRuleResult safe(
            FraudReason reason
    ) {
        return new FraudRuleResult(
                reason,
                false
        );
    }
}