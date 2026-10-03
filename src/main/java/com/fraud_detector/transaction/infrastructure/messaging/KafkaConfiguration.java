package com.fraud_detector.transaction.infrastructure.messaging;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
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
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

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

        return new DefaultKafkaProducerFactory<>(
                java.util.Map.of(
                        "bootstrap.servers",
                        bootstrapServers,
                        "key.serializer",
                        StringSerializer.class,
                        "value.serializer",
                        JacksonJsonSerializer.class
                )
        );
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

        JacksonJsonDeserializer<
                TransactionAnalysisRequestedEvent
                > valueDeserializer =
                new JacksonJsonDeserializer<>(
                        TransactionAnalysisRequestedEvent.class
                );

        valueDeserializer.addTrustedPackages(
                "com.fraud_detector.transaction.infrastructure.messaging.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                java.util.Map.of(
                        "bootstrap.servers",
                        bootstrapServers,
                        "group.id",
                        "fraud-detector-analysis-consumer",
                        "auto.offset.reset",
                        "earliest",
                        "key.deserializer",
                        StringDeserializer.class,
                        "value.deserializer",
                        JacksonJsonDeserializer.class
                ),
                new StringDeserializer(),
                valueDeserializer
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