package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Transaction;

public class UnknownDeviceRule implements FraudRule {

    @Override
    public FraudReason reason() {
        return FraudReason.UNKNOWN_DEVICE;
    }

    @Override
    public FraudRuleResult evaluate(
            Transaction transaction,
            FraudRuleContext context
    ) {
        boolean knownDevice =
                context.knownDeviceIds()
                        .contains(
                                transaction.deviceId()
                        );

        if (knownDevice) {
            return FraudRuleResult.safe(
                    reason()
            );
        }

        return FraudRuleResult.suspicious(
                reason()
        );
    }
}