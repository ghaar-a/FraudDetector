package com.fraud_detector.transaction.infrastructure.messaging.listener;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.infrastructure.messaging.TransactionAnalysisMetrics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionAnalysisKafkaListenerTest {

    private final TransactionApplicationService transactionApplicationService =
            mock(TransactionApplicationService.class);

    private final TransactionAnalysisMapper mapper =
            new TransactionAnalysisMapper();

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    private final TransactionAnalysisMetrics metrics =
            new TransactionAnalysisMetrics(meterRegistry);

    private final TransactionAnalysisKafkaListener listener =
            new TransactionAnalysisKafkaListener(
                    transactionApplicationService,
                    mapper,
                    metrics
            );

    @AfterEach
    void tearDown() {
        org.slf4j.MDC.clear();
        meterRegistry.close();
    }

    @Test
    void shouldConsumeEventAndTriggerTransactionProcessing() {
        TransactionAnalysisRequestedEvent event = sampleEvent();

        listener.onMessage(event);

        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        ArgumentCaptor<FraudRuleContext> contextCaptor =
                ArgumentCaptor.forClass(FraudRuleContext.class);

        verify(transactionApplicationService).process(
                transactionCaptor.capture(),
                contextCaptor.capture()
        );

        Transaction transaction = transactionCaptor.getValue();
        FraudRuleContext context = contextCaptor.getValue();

        assertThat(transaction).isNotNull();
        assertThat(transaction.userId()).isEqualTo("user-123");
        assertThat(transaction.amount()).isEqualTo(
                Money.of(new BigDecimal("149.90"), "BRL")
        );
        assertThat(transaction.merchant()).isEqualTo("Electronics Store");
        assertThat(transaction.category())
                .isEqualTo(TransactionCategory.ELECTRONICS);
        assertThat(transaction.timestamp()).isEqualTo(
                Instant.parse("2026-07-24T12:00:00Z")
        );
        assertThat(transaction.location()).isEqualTo(
                new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        -23.5505,
                        -46.6333
                )
        );
        assertThat(transaction.deviceId()).isEqualTo("device-123");

        assertThat(context).isNotNull();
        assertThat(context.averageTransactionAmount()).isEqualTo(
                Money.of(new BigDecimal("100.00"), "BRL")
        );
        assertThat(context.usualStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(context.usualEndTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(context.knownDeviceIds()).containsExactly("device-123");
        assertThat(context.usualLocation()).isEqualTo(
                new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        -23.5505,
                        -46.6333
                )
        );

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "started").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "success").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "failure").counter().count())
                .isZero();

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing.duration"
        ).timer().count())
                .isEqualTo(1);
    }

    @Test
    void shouldRecordProcessingFailureAndRethrowException() {
        TransactionAnalysisRequestedEvent event = sampleEvent();

        doThrow(new IllegalStateException("Processing failed"))
                .when(transactionApplicationService)
                .process(
                        any(Transaction.class),
                        any(FraudRuleContext.class)
                );

        assertThrows(
                IllegalStateException.class,
                () -> listener.onMessage(event)
        );

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "started").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "failure").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing"
        ).tag("result", "success").counter().count())
                .isZero();

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.processing.duration"
        ).timer().count())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> listener.onMessage(null)
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
                        TransactionCategory.ELECTRONICS,
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