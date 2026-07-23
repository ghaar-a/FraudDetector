package com.fraud_detector.fraud.application;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.fraud.domain.rule.FraudRuleEngine;
import com.fraud_detector.fraud.domain.rule.UnknownDeviceRule;
import com.fraud_detector.fraud.domain.rule.UnusualAmountRule;
import com.fraud_detector.fraud.domain.rule.UnusualTimeRule;
import com.fraud_detector.fraud.domain.service.RiskScoringPolicy;
import com.fraud_detector.fraud.domain.service.RiskScoringService;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudDetectionServiceTest {

    private final FraudDetectionService service =
            createService();

    @Test
    void shouldApproveNormalTransaction() {
        Transaction transaction =
                createTransaction(
                        100.00,
                        "2026-07-22T15:00:00Z",
                        "known-device"
                );

        FraudRuleContext context =
                createContext(
                        100.00,
                        Set.of("known-device")
                );

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        context
                );

        assertEquals(
                FraudDecision.APPROVED,
                analysis.decision()
        );

        assertEquals(
                RiskLevel.LOW,
                analysis.riskLevel()
        );

        assertEquals(
                new BigDecimal("0.0000"),
                analysis.riskScore().value()
        );

        assertTrue(
                analysis.reasons().isEmpty()
        );
    }

    @Test
    void shouldSendSuspiciousTransactionForReview() {
        Transaction transaction =
                createTransaction(
                        600.00,
                        "2026-07-22T15:00:00Z",
                        "unknown-device"
                );

        FraudRuleContext context =
                createContext(
                        100.00,
                        Set.of("known-device")
                );

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        context
                );

        assertEquals(
                FraudDecision.REVIEW,
                analysis.decision()
        );

        assertEquals(
                RiskLevel.HIGH,
                analysis.riskLevel()
        );

        assertEquals(
                new BigDecimal("0.7500"),
                analysis.riskScore().value()
        );

        assertEquals(
                List.of(
                        FraudReason.UNUSUAL_AMOUNT,
                        FraudReason.UNKNOWN_DEVICE
                ),
                analysis.reasons()
        );
    }

    @Test
    void shouldBlockTransactionWithMultipleCriticalSignals() {
        Transaction transaction =
                createTransaction(
                        1000.00,
                        "2026-07-22T03:00:00Z",
                        "unknown-device"
                );

        FraudRuleContext context =
                createContext(
                        100.00,
                        Set.of("known-device")
                );

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        context
                );

        assertEquals(
                FraudDecision.BLOCKED,
                analysis.decision()
        );

        assertEquals(
                RiskLevel.CRITICAL,
                analysis.riskLevel()
        );

        assertEquals(
                new BigDecimal("1.0000"),
                analysis.riskScore().value()
        );

        assertEquals(
                3,
                analysis.reasons().size()
        );

        assertTrue(
                analysis.reasons().contains(
                        FraudReason.UNUSUAL_AMOUNT
                )
        );

        assertTrue(
                analysis.reasons().contains(
                        FraudReason.UNUSUAL_TIME
                )
        );

        assertTrue(
                analysis.reasons().contains(
                        FraudReason.UNKNOWN_DEVICE
                )
        );
    }

    private FraudDetectionService createService() {
        FraudRuleEngine ruleEngine =
                new FraudRuleEngine(
                        List.of(
                                new UnusualAmountRule(),
                                new UnusualTimeRule(),
                                new UnknownDeviceRule()
                        )
                );

        RiskScoringPolicy policy =
                new RiskScoringPolicy(
                        Map.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                BigDecimal.valueOf(0.35),

                                FraudReason.UNUSUAL_TIME,
                                BigDecimal.valueOf(0.25),

                                FraudReason.UNKNOWN_DEVICE,
                                BigDecimal.valueOf(0.40)
                        )
                );

        RiskScoringService scoringService =
                new RiskScoringService(
                        policy
                );

        return new FraudDetectionService(
                ruleEngine,
                scoringService
        );
    }

    private Transaction createTransaction(
            double amount,
            String timestamp,
            String deviceId
    ) {
        return Transaction.create(
                "user-123",
                Money.of(
                        amount,
                        "BRL"
                ),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse(timestamp),
                createLocation(),
                deviceId
        );
    }

    private FraudRuleContext createContext(
            double averageAmount,
            Set<String> knownDevices
    ) {
        return new FraudRuleContext(
                Money.of(
                        averageAmount,
                        "BRL"
                ),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                knownDevices,
                createLocation()
        );
    }

    private TransactionLocation createLocation() {
        return new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );
    }
}