package com.fraud_detector.transaction.infrastructure.messaging.producer;

import com.fraud_detector.shared.observability.CorrelationIdContext;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.TransactionAnalysisMetrics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionAnalysisKafkaProducerTest {

    private final KafkaTemplate<String, TransactionAnalysisRequestedEvent>
            kafkaTemplate = mock(KafkaTemplate.class);

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    private final TransactionAnalysisMetrics metrics =
            new TransactionAnalysisMetrics(meterRegistry);

    private final TransactionAnalysisKafkaProducer producer =
            new TransactionAnalysisKafkaProducer(
                    kafkaTemplate,
                    metrics
            );

    @AfterEach
    void tearDown() {
        org.slf4j.MDC.clear();
        meterRegistry.close();
    }

    @Test
    void shouldPublishEventWithExpectedTopicKeyAndValue() {
        TransactionAnalysisRequestedEvent event = sampleEvent();

        when(kafkaTemplate.send(
                any(ProducerRecord.class)
        )).thenReturn(CompletableFuture.completedFuture(
                mock(SendResult.class)
        ));

        producer.publish(event);

        ArgumentCaptor<ProducerRecord<String, TransactionAnalysisRequestedEvent>>
                recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, TransactionAnalysisRequestedEvent> record =
                recordCaptor.getValue();

        assertThat(record.topic())
                .isEqualTo(KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS);

        assertThat(record.key())
                .isEqualTo("user-123");

        assertThat(record.value())
                .isSameAs(event);

        assertThat(record.headers().lastHeader(
                CorrelationIdContext.HEADER_NAME
        )).isNull();

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.publish"
        ).tag("result", "success").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.publish"
        ).tag("result", "failure").counter().count())
                .isZero();
    }

    @Test
    void shouldIncludeCorrelationIdInKafkaHeader() {
        TransactionAnalysisRequestedEvent event = sampleEvent();
        String correlationId = "test-correlation-123";

        org.slf4j.MDC.put(
                CorrelationIdContext.MDC_KEY,
                correlationId
        );

        when(kafkaTemplate.send(
                any(ProducerRecord.class)
        )).thenReturn(CompletableFuture.completedFuture(
                mock(SendResult.class)
        ));

        producer.publish(event);

        ArgumentCaptor<ProducerRecord<String, TransactionAnalysisRequestedEvent>>
                recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, TransactionAnalysisRequestedEvent> record =
                recordCaptor.getValue();

        assertThat(record.headers().lastHeader(
                CorrelationIdContext.HEADER_NAME
        )).isNotNull();

        assertThat(new String(
                record.headers()
                        .lastHeader(CorrelationIdContext.HEADER_NAME)
                        .value(),
                StandardCharsets.UTF_8
        )).isEqualTo(correlationId);
    }

    @Test
    void shouldRecordPublishFailureWhenKafkaSendFails() {
        TransactionAnalysisRequestedEvent event = sampleEvent();

        when(kafkaTemplate.send(
                any(ProducerRecord.class)
        )).thenReturn(CompletableFuture.failedFuture(
                new IllegalStateException("Kafka unavailable")
        ));

        producer.publish(event);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.publish"
        ).tag("result", "failure").counter().count())
                .isEqualTo(1.0);

        assertThat(meterRegistry.get(
                "fraud_detector.kafka.transaction_analysis.publish"
        ).tag("result", "success").counter().count())
                .isZero();
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> producer.publish(null)
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