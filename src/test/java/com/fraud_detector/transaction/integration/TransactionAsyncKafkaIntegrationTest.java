package com.fraud_detector.transaction.integration;

import com.fraud_detector.fraud.infrastructure.persistence.repository.FraudAnalysisJpaRepository;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.persistence.repository.JpaTransactionRepository;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestRestTemplate
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
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaTransactionRepository transactionRepository;

    @Autowired
    private FraudAnalysisJpaRepository fraudAnalysisRepository;

    @Test
    void shouldProcessAsyncTransactionThroughKafkaAndPersistResults() {

        TransactionAnalysisRequest request =
                createRequest();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<TransactionAnalysisRequest> httpEntity =
                new HttpEntity<>(
                        request,
                        headers
                );

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/v1/transactions/analyze/async",
                        httpEntity,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {

                    assertThat(
                            transactionRepository.findAll()
                    )
                            .anyMatch(
                                    transaction ->
                                            transaction
                                                    .getUserId()
                                                    .equals("user-123")
                            );

                    assertThat(
                            fraudAnalysisRepository.findAll()
                    )
                            .isNotEmpty();
                });
    }

    private TransactionAnalysisRequest createRequest() {

        return new TransactionAnalysisRequest(
                new TransactionAnalysisRequest.TransactionRequest(
                        "user-123",
                        new TransactionAnalysisRequest.MoneyRequest(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionAnalysisRequest.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-123"
                ),
                new TransactionAnalysisRequest.FraudRuleContextRequest(
                        new TransactionAnalysisRequest.MoneyRequest(
                                new BigDecimal("100.00"),
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("device-123"),
                        new TransactionAnalysisRequest.LocationRequest(
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