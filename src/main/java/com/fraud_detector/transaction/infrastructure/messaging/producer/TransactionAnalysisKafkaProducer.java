package com.fraud_detector.transaction.infrastructure.messaging.producer;

import com.fraud_detector.shared.observability.CorrelationIdContext;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.TransactionAnalysisMetrics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class TransactionAnalysisKafkaProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionAnalysisKafkaProducer.class);

    private final KafkaTemplate<String, TransactionAnalysisRequestedEvent>
            kafkaTemplate;

    private final TransactionAnalysisMetrics metrics;

    public TransactionAnalysisKafkaProducer(
            KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate,
            TransactionAnalysisMetrics metrics
    ) {
        this.kafkaTemplate = Objects.requireNonNull(
                kafkaTemplate,
                "Kafka template cannot be null"
        );

        this.metrics = Objects.requireNonNull(
                metrics,
                "Transaction analysis metrics cannot be null"
        );
    }

    public void publish(TransactionAnalysisRequestedEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");

        ProducerRecord<String, TransactionAnalysisRequestedEvent> record =
                new ProducerRecord<>(
                        KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                        event.transaction().userId(),
                        event
                );

        String correlationId =
                MDC.get(CorrelationIdContext.MDC_KEY);

        if (correlationId != null && !correlationId.isBlank()) {
            record.headers().add(
                    CorrelationIdContext.HEADER_NAME,
                    correlationId.getBytes(StandardCharsets.UTF_8)
            );
        }

        LOGGER.info(
                "Publishing transaction analysis request to Kafka; topic={}",
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS
        );

        try {
            kafkaTemplate.send(record).whenComplete((result, exception) -> {
                if (exception == null) {
                    metrics.recordPublishSuccess();
                    LOGGER.debug(
                            "Transaction analysis request published successfully; topic={}",
                            KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS
                    );
                } else {
                    metrics.recordPublishFailure();
                    LOGGER.error(
                            "Failed to publish transaction analysis request; topic={}",
                            KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                            exception
                    );
                }
            });
        } catch (RuntimeException exception) {
            metrics.recordPublishFailure();
            LOGGER.error(
                    "Failed to submit transaction analysis request to Kafka; topic={}",
                    KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                    exception
            );
            throw exception;
        }
    }
}