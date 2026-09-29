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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Testcontainers
@ActiveProfiles("test")
class TransactionAsyncKafkaIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            );

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
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

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
    void shouldRetryFailedMessageAndPublishItToDeadLetterTopic()
            throws Exception {

        TransactionAnalysisRequestedEvent failingEvent =
                createFailingEvent();

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                "user-retry-dlt",
                failingEvent
        ).get();

        verify(
                kafkaListener,
                timeout(20_000)
                        .times(3)
        ).onMessage(any(TransactionAnalysisRequestedEvent.class));

        try (
                Consumer<
                        String,
                        TransactionAnalysisRequestedEvent
                        > consumer =
                        createDltConsumer()
        ) {
            consumer.subscribe(
                    Set.of(
                            KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS_DLT
                    )
            );

            ConsumerRecords<
                    String,
                    TransactionAnalysisRequestedEvent
                    > records = ConsumerRecords.empty();

            long deadline =
                    System.currentTimeMillis() + 20_000;

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

            var dltRecord =
                    records.iterator().next();

            assertThat(dltRecord.key())
                    .isEqualTo("user-retry-dlt");

            assertThat(dltRecord.value())
                    .isNotNull();

            assertThat(
                    dltRecord.value()
                            .transaction()
                            .userId()
            )
                    .isEqualTo("user-retry-dlt");

            assertThat(
                    dltRecord.headers()
                            .lastHeader(
                                    "kafka_dlt-exception-fqcn"
                            )
            )
                    .isNotNull();
        }
    }

    private Consumer<
            String,
            TransactionAnalysisRequestedEvent
            > createDltConsumer() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "fraud-detector-dlt-test-consumer"
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

        JacksonJsonDeserializer<
                TransactionAnalysisRequestedEvent
                > valueDeserializer =
                new JacksonJsonDeserializer<>(
                        TransactionAnalysisRequestedEvent.class
                );

        valueDeserializer.addTrustedPackages(
                "com.fraud_detector.transaction.infrastructure.messaging.event"
        );

        return new KafkaConsumer<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    private TransactionAnalysisRequestedEvent createFailingEvent() {

        return new TransactionAnalysisRequestedEvent(
                new TransactionAnalysisRequestedEvent.TransactionRequest(
                        "user-retry-dlt",
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
                        "device-retry"
                ),
                null
        );
    }
}