package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Transaction;

public interface FraudRule {

    FraudReason reason();

    FraudRuleResult evaluate(
            Transaction transaction,
            FraudRuleContext context
    );
}