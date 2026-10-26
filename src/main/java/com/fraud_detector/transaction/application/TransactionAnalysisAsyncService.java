
package com.fraud_detector.transaction.application;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransactionAnalysisAsyncService {

    private final TransactionAnalysisKafkaProducer kafkaProducer;

    public TransactionAnalysisAsyncService(
            TransactionAnalysisKafkaProducer kafkaProducer
    ) {
        this.kafkaProducer = kafkaProducer;
    }

    public CompletableFuture<Void> requestAnalysis(
            TransactionAnalysisRequestedEvent event
    ) {
        if (event == null) {
            throw new NullPointerException("Transaction analysis event must not be null");
        }

        return kafkaProducer.publish(event)
                .thenApply(sendResult -> (Void) null)
                .exceptionallyCompose(exception ->
                        CompletableFuture.failedFuture(
                                new TransactionAnalysisPublicationException(exception)
                        )
                );
    }
}
