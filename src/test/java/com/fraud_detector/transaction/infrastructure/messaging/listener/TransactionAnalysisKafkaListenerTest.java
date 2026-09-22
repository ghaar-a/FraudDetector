package com.fraud_detector.transaction.infrastructure.messaging.listener;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionAnalysisKafkaListenerTest {

    private final FraudDetectionService fraudDetectionService =
            mock(FraudDetectionService.class);

    private final TransactionAnalysisMapper mapper =
            new TransactionAnalysisMapper();

    private final TransactionAnalysisKafkaListener listener =
            new TransactionAnalysisKafkaListener(
                    fraudDetectionService,
                    mapper
            );

    @Test
    void shouldConsumeEventAndTriggerFraudAnalysis() {
        TransactionAnalysisRequestedEvent event = sampleEvent();

        FraudAnalysis analysis =
                FraudAnalysis.create(
                        com.fraud_detector.transaction.domain.model.TransactionId.generate(),
                        RiskScore.of(new BigDecimal("0.7500")),
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT
                        )
                );

        when(
                fraudDetectionService.analyze(
                        any(),
                        any()
                )
        ).thenReturn(analysis);

        listener.onMessage(event);

        verify(fraudDetectionService, times(1))
                .analyze(any(), any());

        assertThat(event.transaction().userId()).isEqualTo("user-123");
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