package com.fraud_detector.fraud.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record RiskScore(BigDecimal value) {

    private static final BigDecimal MIN_VALUE = BigDecimal.ZERO;
    private static final BigDecimal MAX_VALUE = BigDecimal.ONE;

    public RiskScore {
        Objects.requireNonNull(
                value,
                "Risk score cannot be null"
        );

        if (value.compareTo(MIN_VALUE) < 0
                || value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException(
                    "Risk score must be between 0 and 1"
            );
        }

        value = value.setScale(
                4,
                RoundingMode.HALF_UP
        );
    }

    public static RiskScore of(BigDecimal value) {
        return new RiskScore(value);
    }

    public static RiskScore of(double value) {
        return new RiskScore(
                BigDecimal.valueOf(value)
        );
    }

    public static RiskScore zero() {
        return new RiskScore(BigDecimal.ZERO);
    }

    public static RiskScore maximum() {
        return new RiskScore(BigDecimal.ONE);
    }

    public boolean isLow() {
        return value.compareTo(
                BigDecimal.valueOf(0.30)
        ) <= 0;
    }

    public boolean isMedium() {
        return value.compareTo(
                BigDecimal.valueOf(0.30)
        ) > 0
                && value.compareTo(
                BigDecimal.valueOf(0.70)
        ) <= 0;
    }

    public boolean isHigh() {
        return value.compareTo(
                BigDecimal.valueOf(0.70)
        ) > 0
                && value.compareTo(
                BigDecimal.valueOf(0.90)
        ) <= 0;
    }

    public boolean isCritical() {
        return value.compareTo(
                BigDecimal.valueOf(0.90)
        ) > 0;
    }
}