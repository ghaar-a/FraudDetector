package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;

import java.math.BigDecimal;

public class UnusualAmountRule implements FraudRule {

    private static final BigDecimal DEFAULT_MULTIPLIER =
            BigDecimal.valueOf(5);

    private final BigDecimal multiplier;

    public UnusualAmountRule() {
        this(DEFAULT_MULTIPLIER);
    }

    public UnusualAmountRule(
            BigDecimal multiplier
    ) {
        if (multiplier == null
                || multiplier.compareTo(BigDecimal.ONE) <= 0) {
            throw new IllegalArgumentException(
                    "Multiplier must be greater than 1"
            );
        }

        this.multiplier = multiplier;
    }

    @Override
    public FraudReason reason() {
        return FraudReason.UNUSUAL_AMOUNT;
    }

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRuleContext context
    ) {
        Money averageAmount =
                context.averageTransactionAmount();

        BigDecimal threshold =
                averageAmount.amount()
                        .multiply(multiplier);

        boolean suspicious =
                transaction.amount()
                        .amount()
                        .compareTo(threshold) > 0;

        if (suspicious) {
            return FraudRuleResult.suspicious(
                    reason()
            );
        }

        return FraudRuleResult.safe(
                reason()
        );
    }
}