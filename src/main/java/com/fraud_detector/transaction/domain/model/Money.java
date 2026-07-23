package com.fraud_detector.transaction.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(
        BigDecimal amount,
        String currency
) {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }

        currency = currency.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");

        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot compare money values with different currencies"
            );
        }
    }
}