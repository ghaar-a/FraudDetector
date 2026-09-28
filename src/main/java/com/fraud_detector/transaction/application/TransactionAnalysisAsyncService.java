package com.fraud_detector.transaction.application;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransactionAnalysisAsyncService {

    private final TransactionAnalysisKafkaProducer kafkaProducer;

    public TransactionAnalysisAsyncService(
            TransactionAnalysisKafkaProducer kafkaProducer
    ) {
        this.kafkaProducer = Objects.requireNonNull(
                kafkaProducer,
                "Kafka producer cannot be null"
        );
    }

    public void requestAnalysis(
            TransactionAnalysisRequestedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Transaction analysis event cannot be null"
        );

        kafkaProducer.publish(event);
    }
}