package com.fraud_detector.transaction.infrastructure.messaging;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    private final String bootstrapServers;

    public KafkaConfiguration(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers
    ) {
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public ProducerFactory<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisProducerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisKafkaTemplate(
            ProducerFactory<
                    String,
                    TransactionAnalysisRequestedEvent
                    > transactionAnalysisProducerFactory
    ) {
        return new KafkaTemplate<>(
                transactionAnalysisProducerFactory
        );
    }

    @Bean
    public ConsumerFactory<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisConsumerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "fraud-detector-analysis-consumer"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JacksonJsonDeserializer.class
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(
                        TransactionAnalysisRequestedEvent.class
                )
        );
    }

    @Bean
    public DeadLetterPublishingRecoverer transactionAnalysisDeadLetterPublishingRecoverer(
            KafkaTemplate<
                    String,
                    TransactionAnalysisRequestedEvent
                    > transactionAnalysisKafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(
                transactionAnalysisKafkaTemplate,
                (record, exception) ->
                        new TopicPartition(
                                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS_DLT,
                                record.partition()
                        )
        );
    }

    @Bean
    public DefaultErrorHandler transactionAnalysisKafkaErrorHandler(
            DeadLetterPublishingRecoverer transactionAnalysisDeadLetterPublishingRecoverer
    ) {
        return new DefaultErrorHandler(
                transactionAnalysisDeadLetterPublishingRecoverer,
                new FixedBackOff(
                        0L,
                        2L
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisKafkaListenerContainerFactory(
            ConsumerFactory<
                    String,
                    TransactionAnalysisRequestedEvent
                    > transactionAnalysisConsumerFactory,
            DefaultErrorHandler transactionAnalysisKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<
                String,
                TransactionAnalysisRequestedEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                transactionAnalysisConsumerFactory
        );

        factory.setCommonErrorHandler(
                transactionAnalysisKafkaErrorHandler
        );

        return factory;
    }
}