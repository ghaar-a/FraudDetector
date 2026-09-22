package com.fraud_detector.transaction.infrastructure.messaging;

import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Bean
    public ProducerFactory<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisProducerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
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
                "localhost:9092"
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
    public ConcurrentKafkaListenerContainerFactory<
            String,
            TransactionAnalysisRequestedEvent
            > transactionAnalysisKafkaListenerContainerFactory(
            ConsumerFactory<
                    String,
                    TransactionAnalysisRequestedEvent
                    > transactionAnalysisConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<
                String,
                TransactionAnalysisRequestedEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                transactionAnalysisConsumerFactory
        );

        return factory;
    }
}