package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionApplicationServiceIdempotencyTest {

    private static final String IDEMPOTENCY_KEY = "transaction-request-123";
    private static final String ORIGINAL_FINGERPRINT = "a".repeat(64);
    private static final String DIFFERENT_FINGERPRINT = "b".repeat(64);

    private TransactionRepository transactionRepository;
    private FraudAnalysisRepository fraudAnalysisRepository;
    private TransactionAnalysisProcessor processor;
    private TransactionApplicationService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        fraudAnalysisRepository = mock(FraudAnalysisRepository.class);
        processor = mock(TransactionAnalysisProcessor.class);

        service = new TransactionApplicationService(
                transactionRepository,
                fraudAnalysisRepository,
                processor
        );
    }

    @Test
    void shouldReturnExistingAnalysisForSameIdempotencyKeyAndFingerprint() {
        Transaction existingTransaction = createTransaction();
        FraudAnalysis existingAnalysis = createAnalysis(existingTransaction);

        when(
                transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)
        ).thenReturn(Optional.of(existingTransaction));

        when(
                transactionRepository.findFingerprintByIdempotencyKey(
                        IDEMPOTENCY_KEY
                )
        ).thenReturn(Optional.of(ORIGINAL_FINGERPRINT));

        when(
                fraudAnalysisRepository.findByTransactionId(
                        existingTransaction.id().value()
                )
        ).thenReturn(Optional.of(existingAnalysis));

        FraudAnalysis result = service.process(
                createTransaction(),
                createContext(),
                IDEMPOTENCY_KEY,
                ORIGINAL_FINGERPRINT
        );

        assertSame(existingAnalysis, result);

        verify(transactionRepository)
                .findByIdempotencyKey(IDEMPOTENCY_KEY);

        verify(transactionRepository)
                .findFingerprintByIdempotencyKey(IDEMPOTENCY_KEY);

        verify(fraudAnalysisRepository)
                .findByTransactionId(existingTransaction.id().value());

        verifyNoInteractions(processor);
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentFingerprint() {
        Transaction existingTransaction = createTransaction();

        when(
                transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)
        ).thenReturn(Optional.of(existingTransaction));

        when(
                transactionRepository.findFingerprintByIdempotencyKey(
                        IDEMPOTENCY_KEY
                )
        ).thenReturn(Optional.of(ORIGINAL_FINGERPRINT));

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> service.process(
                        createTransaction(),
                        createContext(),
                        IDEMPOTENCY_KEY,
                        DIFFERENT_FINGERPRINT
                )
        );

        verify(transactionRepository)
                .findByIdempotencyKey(IDEMPOTENCY_KEY);

        verify(transactionRepository)
                .findFingerprintByIdempotencyKey(IDEMPOTENCY_KEY);

        verify(fraudAnalysisRepository, never())
                .findByTransactionId(existingTransaction.id().value());

        verifyNoInteractions(processor);
    }

    @Test
    void shouldRejectBlankIdempotencyKey() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.process(
                        createTransaction(),
                        createContext(),
                        "   ",
                        ORIGINAL_FINGERPRINT
                )
        );

        verifyNoInteractions(
                transactionRepository,
                fraudAnalysisRepository,
                processor
        );
    }

    @Test
    void shouldRejectInvalidFingerprint() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.process(
                        createTransaction(),
                        createContext(),
                        IDEMPOTENCY_KEY,
                        "not-a-sha256-fingerprint"
                )
        );

        verifyNoInteractions(
                transactionRepository,
                fraudAnalysisRepository,
                processor
        );
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "user-123",
                Money.of(new BigDecimal("149.90"), "BRL"),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse("2026-07-24T12:00:00Z"),
                createLocation(),
                "device-123"
        );
    }

    private FraudRuleContext createContext() {
        return new FraudRuleContext(
                Money.of(new BigDecimal("100.00"), "BRL"),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                Set.of("device-123"),
                createLocation()
        );
    }

    private TransactionLocation createLocation() {
        return new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                -23.5505,
                -46.6333
        );
    }

    private FraudAnalysis createAnalysis(Transaction transaction) {
        return FraudAnalysis.create(
                transaction.id(),
                RiskScore.of(new BigDecimal("0.7500")),
                RiskLevel.HIGH,
                FraudDecision.REVIEW,
                List.of(
                        FraudReason.UNUSUAL_AMOUNT,
                        FraudReason.UNKNOWN_DEVICE
                )
        );
    }
}
