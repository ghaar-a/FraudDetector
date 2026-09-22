package com.fraud_detector.transaction.infrastructure.messaging.event;

import com.fraud_detector.transaction.domain.model.TransactionCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

public record TransactionAnalysisRequestedEvent(
        TransactionRequest transaction,
        FraudRuleContextRequest context
) {

    public record TransactionRequest(
            String userId,
            MoneyRequest amount,
            String merchant,
            TransactionCategory category,
            Instant timestamp,
            LocationRequest location,
            String deviceId
    ) {
    }

    public record FraudRuleContextRequest(
            MoneyRequest averageTransactionAmount,
            LocalTime usualStartTime,
            LocalTime usualEndTime,
            Set<String> knownDeviceIds,
            LocationRequest usualLocation
    ) {
    }

    public record MoneyRequest(
            BigDecimal amount,
            String currency
    ) {
    }

    public record LocationRequest(
            String country,
            String state,
            String city,
            Double latitude,
            Double longitude
    ) {
    }
}