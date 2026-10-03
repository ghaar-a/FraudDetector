package com.fraud_detector.transaction.infrastructure.messaging;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfiguration {

    @Bean
    public DeadLetterPublishingRecoverer transactionAnalysisDeadLetterPublishingRecoverer(
            KafkaTemplate<String, TransactionAnalysisRequestedEvent> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new TopicPartition(
                                record.topic() + ".DLT",
                                record.partition()
                        )
        );
    }

    @Bean
    public DefaultErrorHandler transactionAnalysisKafkaErrorHandler(
            DeadLetterPublishingRecoverer transactionAnalysisDeadLetterPublishingRecoverer
    ) {
        FixedBackOff fixedBackOff =
                new FixedBackOff(
                        1000L,
                        2L
                );

        return new DefaultErrorHandler(
                transactionAnalysisDeadLetterPublishingRecoverer,
                fixedBackOff
        );
    }
}