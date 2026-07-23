package com.fraud_detector.fraud.domain.model;

import com.fraud_detector.transaction.domain.model.TransactionId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FraudAnalysisTest {

    @Test
    void shouldCreateFraudAnalysis() {
        TransactionId transactionId =
                TransactionId.generate();

        RiskScore riskScore =
                RiskScore.of(0.85);

        FraudAnalysis analysis =
                FraudAnalysis.create(
                        transactionId,
                        riskScore,
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNUSUAL_TIME
                        )
                );

        assertNotNull(analysis.id());
        assertEquals(
                transactionId,
                analysis.transactionId()
        );
        assertEquals(
                riskScore,
                analysis.riskScore()
        );
        assertEquals(
                RiskLevel.HIGH,
                analysis.riskLevel()
        );
        assertEquals(
                FraudDecision.REVIEW,
                analysis.decision()
        );
        assertEquals(
                2,
                analysis.reasons().size()
        );
        assertNotNull(analysis.analyzedAt());
    }

    @Test
    void shouldIdentifyBlockedAnalysisAsFraudulent() {
        FraudAnalysis analysis =
                FraudAnalysis.create(
                        TransactionId.generate(),
                        RiskScore.of(0.95),
                        RiskLevel.CRITICAL,
                        FraudDecision.BLOCKED,
                        List.of(
                                FraudReason.UNUSUAL_LOCATION
                        )
                );

        assertTrue(analysis.isFraudulent());
    }

    @Test
    void shouldIdentifyReviewDecision() {
        FraudAnalysis analysis =
                FraudAnalysis.create(
                        TransactionId.generate(),
                        RiskScore.of(0.75),
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT
                        )
                );

        assertTrue(
                analysis.requiresManualReview()
        );
    }

    @Test
    void shouldNotExposeMutableReasonsList() {
        FraudAnalysis analysis =
                FraudAnalysis.create(
                        TransactionId.generate(),
                        RiskScore.of(0.80),
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> analysis.reasons().add(
                        FraudReason.UNKNOWN_DEVICE
                )
        );
    }
}