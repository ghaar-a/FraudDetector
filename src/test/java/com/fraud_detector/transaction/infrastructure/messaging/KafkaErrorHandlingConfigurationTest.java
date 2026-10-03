package com.fraud_detector.transaction.infrastructure.messaging;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaErrorHandlingConfigurationTest {

    @Test
    void shouldCreateDeadLetterPublishingRecoverer() {

        KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate =
                mockKafkaTemplate();

        KafkaErrorHandlingConfiguration configuration =
                new KafkaErrorHandlingConfiguration();

        DeadLetterPublishingRecoverer recoverer =
                configuration.transactionAnalysisDeadLetterPublishingRecoverer(
                        kafkaTemplate
                );

        assertThat(recoverer)
                .isNotNull();
    }

    @Test
    void shouldCreateKafkaErrorHandler() {

        KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate =
                mockKafkaTemplate();

        KafkaErrorHandlingConfiguration configuration =
                new KafkaErrorHandlingConfiguration();

        DeadLetterPublishingRecoverer recoverer =
                configuration.transactionAnalysisDeadLetterPublishingRecoverer(
                        kafkaTemplate
                );

        DefaultErrorHandler errorHandler =
                configuration.transactionAnalysisKafkaErrorHandler(
                        recoverer
                );

        assertThat(errorHandler)
                .isNotNull();
    }

    private KafkaTemplate<String, TransactionAnalysisRequestedEvent> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}