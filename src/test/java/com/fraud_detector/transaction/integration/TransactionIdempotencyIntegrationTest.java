package com.fraud_detector.transaction.integration;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    @Autowired
    private TransactionApplicationService transactionApplicationService;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM fraud_analyses");
        jdbcTemplate.update("DELETE FROM transactions");
    }

    @Test
    void shouldPersistOnlyOneTransactionAndAnalysisWhenEventIsRedelivered()
            throws Exception {
        String idempotencyKey = "redelivery-" + UUID.randomUUID();

        TransactionAnalysisRequestedEvent event = createEvent(
                idempotencyKey,
                REQUEST_FINGERPRINT,
                "Electronics Store"
        );

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                idempotencyKey,
                event
        ).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    assertThat(countTransactions(idempotencyKey))
                            .isEqualTo(1);

                    assertThat(countAnalysesForIdempotencyKey(idempotencyKey))
                            .isEqualTo(1);
                });

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
                idempotencyKey,
                event
        ).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(30))
                .during(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(countTransactions(idempotencyKey))
                            .isEqualTo(1);

                    assertThat(countAnalysesForIdempotencyKey(idempotencyKey))
                            .isEqualTo(1);
                });
    }

    @Test
    void shouldPersistOnlyOneTransactionAndAnalysisForConcurrentRequests()
            throws Exception {
        String idempotencyKey = "concurrent-" + UUID.randomUUID();
        int concurrentRequests = 8;

        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(concurrentRequests);

        try {
            List<Future<FraudAnalysis>> futures =
                    java.util.stream.IntStream.range(
                                    0,
                                    concurrentRequests
                            )
                            .mapToObj(index -> executor.submit(() -> {
                                Transaction transaction =
                                        createTransaction();

                                FraudRuleContext context = createContext();

                                ready.countDown();

                                if (!start.await(10, TimeUnit.SECONDS)) {
                                    throw new IllegalStateException(
                                            "Timed out waiting to start concurrent requests"
                                    );
                                }

                                return transactionApplicationService.process(
                                        transaction,
                                        context,
                                        idempotencyKey,
                                        REQUEST_FINGERPRINT
                                );
                            }))
                            .toList();

            assertThat(ready.await(10, TimeUnit.SECONDS))
                    .as("All requests should be ready before they start")
                    .isTrue();

            start.countDown();

            List<FraudAnalysis> results = futures.stream()
                    .map(future -> {
                        try {
                            return future.get(30, TimeUnit.SECONDS);
                        } catch (Exception exception) {
                            throw new AssertionError(
                                    "Concurrent idempotent request failed",
                                    exception
                            );
                        }
                    })
                    .toList();

            assertThat(results).hasSize(concurrentRequests);

            UUID expectedTransactionId =
                    results.getFirst().transactionId().value();

            assertThat(results)
                    .allSatisfy(analysis ->
                            assertThat(analysis.transactionId().value())
                                    .isEqualTo(expectedTransactionId)
                    );

            await()
                    .atMost(Duration.ofSeconds(10))
                    .untilAsserted(() -> {
                        assertThat(countTransactions(idempotencyKey))
                                .isEqualTo(1);

                        assertThat(
                                countAnalysesForIdempotencyKey(idempotencyKey)
                        ).isEqualTo(1);
                    });
        } finally {
            start.countDown();
            executor.shutdownNow();

            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS))
                    .as("Concurrent request executor should terminate")
                    .isTrue();
        }
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "concurrent-user",
                Money.of(new BigDecimal("149.90"), "BRL"),
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse("2026-07-24T12:00:00Z"),
                createLocation(),
                "integration-device"
        );
    }

    private FraudRuleContext createContext() {
        return new FraudRuleContext(
                Money.of(new BigDecimal("100.00"), "BRL"),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                Set.of("integration-device"),
                createLocation()
        );
    }

    private TransactionLocation createLocation() {
        return new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                -23.5505,
                -46.6333
        );
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