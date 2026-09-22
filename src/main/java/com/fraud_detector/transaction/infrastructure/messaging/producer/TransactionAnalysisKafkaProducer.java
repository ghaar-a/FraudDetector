package com.fraud_detector.transaction.infrastructure.messaging.producer;

import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransactionAnalysisKafkaProducer {

    private final KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate;

    public TransactionAnalysisKafkaProducer(
            KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = Objects.requireNonNull(
                kafkaTemplate,
                "Kafka template cannot be null"
        );
    }

    public void publish(
            TransactionAnalysisRequestedEvent event
    ) {
        Objects.requireNonNull(event, "Event cannot be null");

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                event.transaction().userId(),
                event
        );
    }
}