package com.fraud_detector.fraud.domain.service;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {

    private final RiskScoringService service =
            new RiskScoringService(
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
                    )
            );

    @Test
    void shouldApproveLowRiskTransaction() {
        Transaction transaction =
                createTransaction();

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        List.of()
                );

        assertEquals(
                RiskLevel.LOW,
                analysis.riskLevel()
        );

        assertEquals(
                FraudDecision.APPROVED,
                analysis.decision()
        );

        assertEquals(
                BigDecimal.ZERO.setScale(4),
                analysis.riskScore().value()
        );
    }

    @Test
    void shouldSendMediumRiskTransactionForReview() {
        Transaction transaction =
                createTransaction();

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNUSUAL_TIME
                        )
                );

        assertEquals(
                RiskLevel.MEDIUM,
                analysis.riskLevel()
        );

        assertEquals(
                FraudDecision.REVIEW,
                analysis.decision()
        );

        assertEquals(
                new BigDecimal("0.5000"),
                analysis.riskScore().value()
        );
    }

    @Test
    void shouldBlockCriticalRiskTransaction() {
        Transaction transaction =
                createTransaction();

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        List.of(
                                FraudReason.UNUSUAL_LOCATION,
                                FraudReason.UNKNOWN_DEVICE,
                                FraudReason.UNUSUAL_AMOUNT
                        )
                );

        assertEquals(
                RiskLevel.CRITICAL,
                analysis.riskLevel()
        );

        assertEquals(
                FraudDecision.BLOCKED,
                analysis.decision()
        );

        assertTrue(
                analysis.isFraudulent()
        );
    }

    @Test
    void shouldPreserveFraudReasonsInAnalysis() {
        Transaction transaction =
                createTransaction();

        List<FraudReason> reasons =
                List.of(
                        FraudReason.UNUSUAL_AMOUNT,
                        FraudReason.UNKNOWN_DEVICE
                );

        FraudAnalysis analysis =
                service.analyze(
                        transaction,
                        reasons
                );

        assertEquals(
                reasons,
                analysis.reasons()
        );
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "user-123",
                Money.of(
                        500.00,
                        "BRL"
                ),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse(
                        "2026-07-22T15:00:00Z"
                ),
                new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        null,
                        null
                ),
                "device-123"
        );
    }
}