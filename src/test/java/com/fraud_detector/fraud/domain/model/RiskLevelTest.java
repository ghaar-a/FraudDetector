package com.fraud_detector.fraud.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskLevelTest {

    @Test
    void shouldContainExpectedRiskLevels() {
        assertArrayEquals(
                new RiskLevel[]{
                        RiskLevel.LOW,
                        RiskLevel.MEDIUM,
                        RiskLevel.HIGH,
                        RiskLevel.CRITICAL
                },
                RiskLevel.values()
        );
    }
}