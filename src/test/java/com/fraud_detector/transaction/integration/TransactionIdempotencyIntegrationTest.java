package com.fraud_detector.transaction.integration;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.listener.missing-topics-fatal=false",
        "spring.kafka.admin.auto-create=true",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class TransactionIdempotencyIntegrationTest {

    private static final String IDEMPOTENCY_KEY =
            "integration-" + UUID.randomUUID();

    private static final String REQUEST_FINGERPRINT = "a".repeat(64);

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.7.1")
            );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.kafka.listener.auto-startup",
                () -> true
        );

        registry.add(
                "spring.kafka.admin.auto-create",
                () -> true
        );
    }

    @Autowired
    private KafkaTemplate<String, TransactionAnalysisRequestedEvent>
            kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM fraud_analyses");
        jdbcTemplate.update("DELETE FROM transactions");
    }

    @Test
    void shouldPersistOnlyOneTransactionAndAnalysisWhenEventIsRedelivered()
            throws Exception {
        TransactionAnalysisRequestedEvent event = createEvent(
                IDEMPOTENCY_KEY,
                REQUEST_FINGERPRINT,
                "Electronics Store"
        );

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                IDEMPOTENCY_KEY,
                event
        ).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    assertThat(countTransactions(IDEMPOTENCY_KEY))
                            .isEqualTo(1);

                    assertThat(countAnalysesForIdempotencyKey(
                            IDEMPOTENCY_KEY
                    )).isEqualTo(1);
                });

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                IDEMPOTENCY_KEY,
                event
        ).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(30))
                .during(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(countTransactions(IDEMPOTENCY_KEY))
                            .isEqualTo(1);

                    assertThat(countAnalysesForIdempotencyKey(
                            IDEMPOTENCY_KEY
                    )).isEqualTo(1);
                });
    }

    private long countTransactions(String idempotencyKey) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM transactions
                WHERE idempotency_key = ?
                """,
                Long.class,
                idempotencyKey
        );

        return count == null ? 0 : count;
    }

    private long countAnalysesForIdempotencyKey(String idempotencyKey) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM fraud_analyses analysis
                JOIN transactions transaction_record
                  ON transaction_record.id = analysis.transaction_id
                WHERE transaction_record.idempotency_key = ?
                """,
                Long.class,
                idempotencyKey
        );

        return count == null ? 0 : count;
    }

    private TransactionAnalysisRequestedEvent createEvent(
            String idempotencyKey,
            String fingerprint,
            String merchant
    ) {
        return new TransactionAnalysisRequestedEvent(
                new TransactionAnalysisRequestedEvent.TransactionRequest(
                        "integration-user-" + UUID.randomUUID(),
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        merchant,
                        TransactionCategory.ELECTRONICS,
                        Instant.parse("2026-07-24T12:00:00Z"),
                        new TransactionAnalysisRequestedEvent.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "integration-device"
                ),
                new TransactionAnalysisRequestedEvent.FraudRuleContextRequest(
                        new TransactionAnalysisRequestedEvent.MoneyRequest(
                                new BigDecimal("100.00"),
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("integration-device"),
                        new TransactionAnalysisRequestedEvent.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                ),
                idempotencyKey,
                fingerprint
        );
    }
}