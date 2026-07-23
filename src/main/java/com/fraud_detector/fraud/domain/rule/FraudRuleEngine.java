package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Transaction;

import java.util.List;
import java.util.Objects;

public class FraudRuleEngine {

    private final List<FraudRule> rules;

    public FraudRuleEngine(
            List<FraudRule> rules
    ) {
        Objects.requireNonNull(
                rules,
                "Fraud rules cannot be null"
        );

        if (rules.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one fraud rule is required"
            );
        }

        this.rules = List.copyOf(rules);
    }

    public List<FraudReason> evaluate(
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

        return rules.stream()
                .map(rule ->
                        rule.evaluate(
                                transaction,
                                context
                        )
                )
                .filter(FraudRuleResult::suspicious)
                .map(FraudRuleResult::reason)
                .toList();
    }
}