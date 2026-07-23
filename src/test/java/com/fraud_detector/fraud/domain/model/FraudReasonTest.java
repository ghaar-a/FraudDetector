package com.fraud_detector.fraud.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraudReasonTest {

    @Test
    void shouldContainExpectedFraudReasons() {
        assertArrayEquals(
                new FraudReason[]{
                        FraudReason.UNUSUAL_AMOUNT,
                        FraudReason.UNUSUAL_LOCATION,
                        FraudReason.IMPOSSIBLE_TRAVEL,
                        FraudReason.UNUSUAL_TIME,
                        FraudReason.HIGH_TRANSACTION_VELOCITY,
                        FraudReason.UNKNOWN_DEVICE,
                        FraudReason.UNUSUAL_MERCHANT_CATEGORY,
                        FraudReason.BEHAVIORAL_DEVIATION
                },
                FraudReason.values()
        );
    }
}