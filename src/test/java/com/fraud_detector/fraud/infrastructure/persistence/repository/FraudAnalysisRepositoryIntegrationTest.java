package com.fraud_detector.fraud.infrastructure.persistence.repository;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.support.PostgresTestConfiguration;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class FraudAnalysisRepositoryIntegrationTest {

    @Autowired
    private FraudAnalysisRepository fraudAnalysisRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveAndRetrieveAnalysisWithMultipleReasons() {
        Transaction transaction = createTransaction();
        transactionRepository.save(transaction);

        FraudAnalysis analysis =
                FraudAnalysis.create(
                        transaction.id(),
                        RiskScore.of(new BigDecimal("0.7500")),
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNKNOWN_DEVICE
                        )
                );

        FraudAnalysis saved = fraudAnalysisRepository.save(analysis);

        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(analysis.id());
        assertThat(saved.transactionId()).isEqualTo(transaction.id());
        assertThat(saved.riskScore()).isEqualTo(analysis.riskScore());
        assertThat(saved.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(saved.decision()).isEqualTo(FraudDecision.REVIEW);
        assertThat(saved.reasons()).containsExactly(
                FraudReason.UNUSUAL_AMOUNT,
                FraudReason.UNKNOWN_DEVICE
        );
        assertThat(saved.analyzedAt()).isEqualTo(analysis.analyzedAt());

        FraudAnalysis retrieved =
                fraudAnalysisRepository.findById(analysis.id()).orElseThrow();

        assertThat(retrieved).isEqualTo(analysis);
    }

    @Test
    void shouldSaveAnalysisWithoutFraudReasons() {
        Transaction transaction = createTransaction();
        transactionRepository.save(transaction);

        FraudAnalysis analysis =
                FraudAnalysis.create(
                        transaction.id(),
                        RiskScore.zero(),
                        RiskLevel.LOW,
                        FraudDecision.APPROVED,
                        List.of()
                );

        FraudAnalysis saved = fraudAnalysisRepository.save(analysis);

        assertThat(saved).isNotNull();
        assertThat(saved.reasons()).isEmpty();

        FraudAnalysis retrieved =
                fraudAnalysisRepository.findById(analysis.id()).orElseThrow();

        assertThat(retrieved.reasons()).isEmpty();
        assertThat(retrieved.decision()).isEqualTo(FraudDecision.APPROVED);
        assertThat(retrieved.riskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(retrieved.riskScore()).isEqualTo(RiskScore.zero());
    }

    @Test
    void shouldFindAnalysisByTransactionId() {
        Transaction transaction = createTransaction();
        transactionRepository.save(transaction);

        FraudAnalysis analysis =
                FraudAnalysis.create(
                        transaction.id(),
                        RiskScore.maximum(),
                        RiskLevel.CRITICAL,
                        FraudDecision.BLOCKED,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNUSUAL_TIME,
                                FraudReason.UNKNOWN_DEVICE
                        )
                );

        fraudAnalysisRepository.save(analysis);

        FraudAnalysis retrieved =
                fraudAnalysisRepository
                        .findByTransactionId(transaction.id().value())
                        .orElseThrow();

        assertThat(retrieved.id()).isEqualTo(analysis.id());
        assertThat(retrieved.transactionId()).isEqualTo(transaction.id());
        assertThat(retrieved.decision()).isEqualTo(FraudDecision.BLOCKED);
        assertThat(retrieved.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(retrieved.riskScore()).isEqualTo(RiskScore.maximum());
        assertThat(retrieved.reasons()).containsExactlyInAnyOrder(
                FraudReason.UNUSUAL_AMOUNT,
                FraudReason.UNUSUAL_TIME,
                FraudReason.UNKNOWN_DEVICE
        );
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "user-123",
                Money.of(149.90, "BRL"),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse("2026-07-24T12:00:00Z"),
                new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        -23.5505,
                        -46.6333
                ),
                "device-123"
        );
    }
}