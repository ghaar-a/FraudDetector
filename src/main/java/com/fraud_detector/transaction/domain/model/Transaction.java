package com.fraud_detector.transaction.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Transaction(
        TransactionId id,
        String userId,
        Money amount,
        String merchant,
        TransactionCategory category,
        Instant timestamp,
        TransactionLocation location,
        String deviceId
) {

    public Transaction {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(merchant, "Merchant cannot be null");
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(deviceId, "Device ID cannot be null");

        validateText(userId, "User ID");
        validateText(merchant, "Merchant");
        validateText(deviceId, "Device ID");
    }

    private static void validateText(
            String value,
            String fieldName
    ) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }

    public static Transaction create(
            String userId,
            Money amount,
            String merchant,
            TransactionCategory category,
            Instant timestamp,
            TransactionLocation location,
            String deviceId
    ) {
        return new Transaction(
                TransactionId.generate(),
                userId,
                amount,
                merchant,
                category,
                timestamp,
                location,
                deviceId
        );
    }
}