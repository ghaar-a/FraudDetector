package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionApplicationServiceTest {

    private TransactionRepository transactionRepository;
    private FraudAnalysisRepository fraudAnalysisRepository;
    private FraudDetectionService fraudDetectionService;
    private TransactionAnalysisKafkaProducer transactionAnalysisKafkaProducer;

    private TransactionApplicationService transactionApplicationService;

    @BeforeEach
    void setUp() {
        transactionRepository =
                mock(TransactionRepository.class);

        fraudAnalysisRepository =
                mock(FraudAnalysisRepository.class);

        fraudDetectionService =
                mock(FraudDetectionService.class);

        transactionAnalysisKafkaProducer =
                mock(TransactionAnalysisKafkaProducer.class);

        transactionApplicationService =
                new TransactionApplicationService(
                        transactionRepository,
                        fraudAnalysisRepository,
                        fraudDetectionService,
                        transactionAnalysisKafkaProducer
                );
    }

    @Test
    void shouldPublishEventWhenAsyncAnalysisIsRequested() {
        TransactionAnalysisRequestedEvent event =
                createEvent();

        transactionApplicationService.requestAsyncAnalysis(event);

        verify(transactionAnalysisKafkaProducer)
                .publish(event);
    }

    private TransactionAnalysisRequestedEvent createEvent() {
        return new TransactionAnalysisRequestedEvent(
                new TransactionAnalysisRequestedEvent.TransactionRequest(
                        "user-123",
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new java.math.BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        com.fraud_detector.transaction.domain.model.TransactionCategory.ELECTRONICS,
                        java.time.Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionAnalysisRequestedEvent.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-123"
                ),
                new TransactionAnalysisRequestedEvent.FraudRuleContextRequest(
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new java.math.BigDecimal("100.00"),
                                "BRL"
                        ),
                        java.time.LocalTime.of(8, 0),
                        java.time.LocalTime.of(22, 0),
                        java.util.Set.of("device-123"),
                        new TransactionAnalysisRequestedEvent.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                )
        );
    }
}