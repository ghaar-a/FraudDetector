package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Transaction;

import java.time.LocalTime;

public class UnusualTimeRule implements FraudRule {

    @Override
    public FraudReason reason() {
        return FraudReason.UNUSUAL_TIME;
    }

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRuleContext context
    ) {
        LocalTime transactionTime =
                transaction.timestamp()
                        .atZone(java.time.ZoneOffset.UTC)
                        .toLocalTime();

        LocalTime start =
                context.usualStartTime();

        LocalTime end =
                context.usualEndTime();

        boolean withinUsualPeriod =
                isWithinPeriod(
                        transactionTime,
                        start,
                        end
                );

        if (withinUsualPeriod) {
            return FraudRuleResult.safe(
                    reason()
            );
        }

        return FraudRuleResult.suspicious(
                reason()
        );
    }

    private boolean isWithinPeriod(
            LocalTime time,
            LocalTime start,
            LocalTime end
    ) {
        if (start.isBefore(end)
                || start.equals(end)) {

            return !time.isBefore(start)
                    && !time.isAfter(end);
        }

        return !time.isBefore(start)
                || !time.isAfter(end);
    }
}