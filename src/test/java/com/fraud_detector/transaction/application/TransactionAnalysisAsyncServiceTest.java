package com.fraud_detector.transaction.application;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionAnalysisAsyncServiceTest {

    private final TransactionAnalysisKafkaProducer kafkaProducer =
            mock(TransactionAnalysisKafkaProducer.class);

    private final TransactionAnalysisAsyncService asyncService =
            new TransactionAnalysisAsyncService(kafkaProducer);

    @Test
    void shouldRequestAnalysisByPublishingEvent() {
        TransactionAnalysisRequestedEvent event =
                sampleEvent();

        asyncService.requestAnalysis(event);

        verify(kafkaProducer).publish(event);
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> asyncService.requestAnalysis(null)
        );
    }

    private TransactionAnalysisRequestedEvent sampleEvent() {
        return new TransactionAnalysisRequestedEvent(
                new TransactionAnalysisRequestedEvent.TransactionRequest(
                        "user-123",
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        com.fraud_detector.transaction.domain.model.TransactionCategory.ELECTRONICS,
                        Instant.parse("2026-07-24T12:00:00Z"),
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
                                new BigDecimal("100.00"),
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("device-123"),
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