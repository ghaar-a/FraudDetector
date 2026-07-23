package com.fraud_detector.fraud.domain.service;

import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringPolicyTest {

    private final RiskScoringPolicy policy =
            new RiskScoringPolicy(
                    Map.of(
                            FraudReason.UNUSUAL_AMOUNT,
                            BigDecimal.valueOf(0.30),

                            FraudReason.UNUSUAL_TIME,
                            BigDecimal.valueOf(0.20),

                            FraudReason.UNKNOWN_DEVICE,
                            BigDecimal.valueOf(0.40),

                            FraudReason.UNUSUAL_LOCATION,
                            BigDecimal.valueOf(0.50)
                    )
            );

    @Test
    void shouldCalculateRiskScoreFromFraudReasons() {
        RiskScore riskScore =
                policy.calculateRiskScore(
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNUSUAL_TIME
                        )
                );

        assertEquals(
                new BigDecimal("0.5000"),
                riskScore.value()
        );
    }

    @Test
    void shouldCapRiskScoreAtOne() {
        RiskScore riskScore =
                policy.calculateRiskScore(
                        List.of(
                                FraudReason.UNUSUAL_LOCATION,
                                FraudReason.UNKNOWN_DEVICE,
                                FraudReason.UNUSUAL_AMOUNT
                        )
                );

        assertEquals(
                new BigDecimal("1.0000"),
                riskScore.value()
        );
    }

    @Test
    void shouldIgnoreReasonsWithoutConfiguredWeight() {
        RiskScore riskScore =
                policy.calculateRiskScore(
                        List.of(
                                FraudReason.BEHAVIORAL_DEVIATION
                        )
                );

        assertEquals(
                new BigDecimal("0.0000"),
                riskScore.value()
        );
    }

    @Test
    void shouldDetermineLowRiskLevel() {
        assertEquals(
                RiskLevel.LOW,
                policy.determineRiskLevel(
                        RiskScore.of(0.20)
                )
        );
    }

    @Test
    void shouldDetermineMediumRiskLevel() {
        assertEquals(
                RiskLevel.MEDIUM,
                policy.determineRiskLevel(
                        RiskScore.of(0.50)
                )
        );
    }

    @Test
    void shouldDetermineHighRiskLevel() {
        assertEquals(
                RiskLevel.HIGH,
                policy.determineRiskLevel(
                        RiskScore.of(0.80)
                )
        );
    }

    @Test
    void shouldDetermineCriticalRiskLevel() {
        assertEquals(
                RiskLevel.CRITICAL,
                policy.determineRiskLevel(
                        RiskScore.of(0.95)
                )
        );
    }

    @Test
    void shouldApproveLowRisk() {
        assertEquals(
                FraudDecision.APPROVED,
                policy.determineDecision(
                        RiskLevel.LOW
                )
        );
    }

    @Test
    void shouldSendMediumRiskForReview() {
        assertEquals(
                FraudDecision.REVIEW,
                policy.determineDecision(
                        RiskLevel.MEDIUM
                )
        );
    }

    @Test
    void shouldSendHighRiskForReview() {
        assertEquals(
                FraudDecision.REVIEW,
                policy.determineDecision(
                        RiskLevel.HIGH
                )
        );
    }

    @Test
    void shouldBlockCriticalRisk() {
        assertEquals(
                FraudDecision.BLOCKED,
                policy.determineDecision(
                        RiskLevel.CRITICAL
                )
        );
    }

    @Test
    void shouldRejectEmptyWeightConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskScoringPolicy(
                        Map.of()
                )
        );
    }

    @Test
    void shouldRejectNegativeWeight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskScoringPolicy(
                        Map.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                BigDecimal.valueOf(-0.1)
                        )
                )
        );
    }
}