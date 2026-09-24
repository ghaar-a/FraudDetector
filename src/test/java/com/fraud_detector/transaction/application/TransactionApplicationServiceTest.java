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
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TransactionApplicationServiceTest {

    private final TransactionRepository transactionRepository =
            mock(TransactionRepository.class);

    private final FraudAnalysisRepository fraudAnalysisRepository =
            mock(FraudAnalysisRepository.class);

    private final FraudDetectionService fraudDetectionService =
            mock(FraudDetectionService.class);

    private final TransactionAnalysisKafkaProducer transactionAnalysisKafkaProducer =
            mock(TransactionAnalysisKafkaProducer.class);

    private final TransactionApplicationService service =
            new TransactionApplicationService(
                    transactionRepository,
                    fraudAnalysisRepository,
                    fraudDetectionService,
                    transactionAnalysisKafkaProducer
            );

    @Test
    void shouldPublishTransactionAnalysisRequest() {
        Transaction transaction = createTransaction();
        FraudRuleContext context = createContext();

        service.requestAnalysis(
                transaction,
                context
        );

        ArgumentCaptor<TransactionAnalysisRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        TransactionAnalysisRequestedEvent.class
                );

        verify(transactionAnalysisKafkaProducer)
                .publish(eventCaptor.capture());

        TransactionAnalysisRequestedEvent event =
                eventCaptor.getValue();

        assertThat(event).isNotNull();

        assertThat(event.transaction().userId())
                .isEqualTo("user-123");

        assertThat(event.transaction().merchant())
                .isEqualTo("Electronics Store");

        assertThat(event.transaction().category())
                .isEqualTo(TransactionCategory.ELECTRONICS);

        assertThat(event.transaction().amount().amount())
                .isEqualByComparingTo("149.90");

        assertThat(event.transaction().amount().currency())
                .isEqualTo("BRL");

        assertThat(event.transaction().deviceId())
                .isEqualTo("device-123");

        assertThat(event.context().averageTransactionAmount().amount())
                .isEqualByComparingTo("100.00");

        assertThat(event.context().averageTransactionAmount().currency())
                .isEqualTo("BRL");

        assertThat(event.context().usualStartTime())
                .isEqualTo(LocalTime.of(8, 0));

        assertThat(event.context().usualEndTime())
                .isEqualTo(LocalTime.of(22, 0));

        assertThat(event.context().knownDeviceIds())
                .containsExactly("device-123");

        verifyNoInteractions(
                transactionRepository,
                fraudAnalysisRepository,
                fraudDetectionService
        );
    }

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

        assertSame(
                expectedAnalysis,
                result
        );

        InOrder inOrder =
                inOrder(
                        transactionRepository,
                        fraudDetectionService,
                        fraudAnalysisRepository
                );

        inOrder.verify(transactionRepository)
                .save(transaction);

        inOrder.verify(fraudDetectionService)
                .analyze(
                        transaction,
                        context
                );

        inOrder.verify(fraudAnalysisRepository)
                .save(expectedAnalysis);

        verifyNoMoreInteractions(
                transactionRepository,
                fraudDetectionService,
                fraudAnalysisRepository
        );
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "user-123",
                Money.of(
                        149.90,
                        "BRL"
                ),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse(
                        "2026-07-24T12:00:00Z"
                ),
                createLocation(),
                "device-123"
        );
    }

    private FraudRuleContext createContext() {
        return new FraudRuleContext(
                Money.of(
                        100.00,
                        "BRL"
                ),
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