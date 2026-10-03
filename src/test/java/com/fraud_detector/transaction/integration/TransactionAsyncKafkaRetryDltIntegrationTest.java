package com.fraud_detector.transaction.integration;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.listener.TransactionAnalysisKafkaListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TransactionAsyncKafkaRetryDltIntegrationTest {


    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse(
                            "confluentinc/cp-kafka:7.6.1"
                    )
            );

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.kafka.listener.auto-startup",
                () -> "true"
        );

        registry.add(
                "spring.kafka.admin.auto-create",
                () -> "true"
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );
    }

    @Autowired
    private KafkaTemplate<
            String,
            TransactionAnalysisRequestedEvent
            > kafkaTemplate;

    @MockitoSpyBean
    private TransactionAnalysisKafkaListener kafkaListener;

    @Test
    void shouldPublishFailedMessageToDeadLetterTopic() {

        String deadLetterTopic =
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS_DLT;

        doThrow(
                new IllegalStateException(
                        "Simulated Kafka processing failure"
                )
        )
                .when(kafkaListener)
                .onMessage(
                        any(TransactionAnalysisRequestedEvent.class)
                );

        TransactionAnalysisRequestedEvent event =
                createEvent();

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                event.transaction().userId(),
                event
        );

        kafkaTemplate.flush();

        verify(
                kafkaListener,
                timeout(20_000)
                        .times(3)
        )
                .onMessage(
                        any(TransactionAnalysisRequestedEvent.class)
                );

        Map<String, Object> consumerProperties =
                new HashMap<>();

        consumerProperties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        consumerProperties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "fraud-detector-dlt-test"
        );

        consumerProperties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        consumerProperties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        consumerProperties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        try (
                Consumer<String, String> consumer =
                        new DefaultKafkaConsumerFactory<
                                String,
                                String
                                >(consumerProperties)
                                .createConsumer()
        ) {

            consumer.subscribe(
                    Set.of(deadLetterTopic)
            );

            ConsumerRecords<String, String> records =
                    ConsumerRecords.empty();

            long deadline =
                    System.currentTimeMillis() + 30_000;

            while (
                    records.isEmpty()
                            && System.currentTimeMillis() < deadline
            ) {
                records = consumer.poll(
                        Duration.ofMillis(500)
                );
            }

            assertThat(records)
                    .isNotEmpty();

            assertThat(records.count())
                    .isGreaterThan(0);

            var dltRecord =
                    records.iterator().next();

            assertThat(dltRecord.key())
                    .isEqualTo("user-dlt-test");

            assertThat(dltRecord.value())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    private TransactionAnalysisRequestedEvent createEvent() {

        return new TransactionAnalysisRequestedEvent(
                new TransactionAnalysisRequestedEvent.TransactionRequest(
                        "user-dlt-test",
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionAnalysisRequestedEvent.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-dlt-test"
                ),
                new TransactionAnalysisRequestedEvent.FraudRuleContextRequest(
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new BigDecimal("100.00"),
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("device-dlt-test"),
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
