package com.fraud_detector.transaction.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "Transaction ID cannot be null");
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    public static TransactionId of(String value) {
        Objects.requireNonNull(value, "Transaction ID cannot be null");
        return new TransactionId(UUID.fromString(value));
    }
}