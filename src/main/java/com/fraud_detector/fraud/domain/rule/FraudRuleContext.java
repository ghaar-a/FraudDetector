package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.TransactionLocation;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public record FraudRuleContext(
        Money averageTransactionAmount,
        LocalTime usualStartTime,
        LocalTime usualEndTime,
        Set<String> knownDeviceIds,
        TransactionLocation usualLocation
) {

    public FraudRuleContext {
        Objects.requireNonNull(
                averageTransactionAmount,
                "Average transaction amount cannot be null"
        );

        Objects.requireNonNull(
                usualStartTime,
                "Usual start time cannot be null"
        );

        Objects.requireNonNull(
                usualEndTime,
                "Usual end time cannot be null"
        );

        Objects.requireNonNull(
                knownDeviceIds,
                "Known device IDs cannot be null"
        );

        knownDeviceIds = Set.copyOf(knownDeviceIds);
    }
}