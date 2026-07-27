package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.application.FraudDetectionService;
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
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TransactionApplicationServiceTest {

    private final TransactionRepository transactionRepository =
            mock(TransactionRepository.class);

    private final FraudAnalysisRepository fraudAnalysisRepository =
            mock(FraudAnalysisRepository.class);

    private final FraudDetectionService fraudDetectionService =
            mock(FraudDetectionService.class);

    private final TransactionApplicationService service =
            new TransactionApplicationService(
                    transactionRepository,
                    fraudAnalysisRepository,
                    fraudDetectionService
            );

    @Test
    void shouldPersistTransactionAnalyzeAndPersistFraudAnalysis() {
        Transaction transaction = createTransaction();
        FraudRuleContext context = createContext();

        FraudAnalysis expectedAnalysis =
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

        when(
                fraudDetectionService.analyze(
                        transaction,
                        context
                )
        ).thenReturn(expectedAnalysis);

        when(
                fraudAnalysisRepository.save(
                        expectedAnalysis
                )
        ).thenReturn(expectedAnalysis);

        FraudAnalysis result =
                service.process(
                        transaction,
                        context
                );

        assertSame(expectedAnalysis, result);

        InOrder inOrder =
                inOrder(
                        transactionRepository,
                        fraudDetectionService,
                        fraudAnalysisRepository
                );

        inOrder.verify(transactionRepository).save(transaction);
        inOrder.verify(fraudDetectionService).analyze(transaction, context);
        inOrder.verify(fraudAnalysisRepository).save(expectedAnalysis);

        verifyNoMoreInteractions(
                transactionRepository,
                fraudDetectionService,
                fraudAnalysisRepository
        );
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "user-123",
                Money.of(149.90, "BRL"),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse("2026-07-24T12:00:00Z"),
                createLocation(),
                "device-123"
        );
    }

    private FraudRuleContext createContext() {
        return new FraudRuleContext(
                Money.of(100.00, "BRL"),
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
}