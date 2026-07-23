package com.fraud_detector.fraud.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreTest {

    @Test
    void shouldCreateValidRiskScore() {
        RiskScore riskScore = RiskScore.of(
                new BigDecimal("0.75")
        );

        assertEquals(
                new BigDecimal("0.7500"),
                riskScore.value()
        );
    }

    @Test
    void shouldCreateRiskScoreFromDouble() {
        RiskScore riskScore = RiskScore.of(0.85);

        assertEquals(
                new BigDecimal("0.8500"),
                riskScore.value()
        );
    }

    @Test
    void shouldCreateZeroRiskScore() {
        RiskScore riskScore = RiskScore.zero();

        assertEquals(
                new BigDecimal("0.0000"),
                riskScore.value()
        );
    }

    @Test
    void shouldCreateMaximumRiskScore() {
        RiskScore riskScore = RiskScore.maximum();

        assertEquals(
                new BigDecimal("1.0000"),
                riskScore.value()
        );
    }

    @Test
    void shouldRejectNegativeRiskScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RiskScore.of(-0.01)
        );
    }

    @Test
    void shouldRejectRiskScoreAboveOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RiskScore.of(1.01)
        );
    }

    @Test
    void shouldIdentifyLowRisk() {
        RiskScore riskScore = RiskScore.of(0.30);

        assertTrue(riskScore.isLow());
    }

    @Test
    void shouldIdentifyMediumRisk() {
        RiskScore riskScore = RiskScore.of(0.50);

        assertTrue(riskScore.isMedium());
    }

    @Test
    void shouldIdentifyHighRisk() {
        RiskScore riskScore = RiskScore.of(0.80);

        assertTrue(riskScore.isHigh());
    }

    @Test
    void shouldIdentifyCriticalRisk() {
        RiskScore riskScore = RiskScore.of(0.95);

        assertTrue(riskScore.isCritical());
    }
}