package com.fraud_detector.transaction.infrastructure.messaging.listener;

import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionAnalysisKafkaListenerTest {

    private final TransactionApplicationService transactionApplicationService =
            mock(TransactionApplicationService.class);

    private final TransactionAnalysisMapper mapper =
            new TransactionAnalysisMapper();

    private final TransactionAnalysisKafkaListener listener =
            new TransactionAnalysisKafkaListener(
                    transactionApplicationService,
                    mapper
            );

    @Test
    void shouldConsumeEventAndTriggerTransactionProcessing() {
        TransactionAnalysisRequestedEvent event =
                sampleEvent();

        listener.onMessage(event);

        verify(transactionApplicationService)
                .process(
                        any(),
                        any()
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
                        Instant.parse(
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