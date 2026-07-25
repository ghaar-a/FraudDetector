package com.fraud_detector.transaction.infrastructure.persistence.repository;

import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransactionRepositoryIntegrationTest {

    @Autowired
    private TransactionRepositoryAdapter transactionRepository;

    @Test
    void shouldSaveAndRetrieveTransaction() {

        Transaction transaction =
                Transaction.create(
                        "user-123",
                        new Money(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionLocation(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-123"
                );

        Transaction saved =
                transactionRepository.save(
                        transaction
                );

        assertThat(saved)
                .isNotNull();

        assertThat(saved.id())
                .isEqualTo(
                        transaction.id()
                );

        assertThat(saved.userId())
                .isEqualTo(
                        transaction.userId()
                );

        assertThat(saved.amount())
                .isEqualTo(
                        transaction.amount()
                );

        assertThat(saved.merchant())
                .isEqualTo(
                        transaction.merchant()
                );

        assertThat(saved.category())
                .isEqualTo(
                        transaction.category()
                );

        assertThat(saved.timestamp())
                .isEqualTo(
                        transaction.timestamp()
                );

        assertThat(saved.location())
                .isEqualTo(
                        transaction.location()
                );

        assertThat(saved.deviceId())
                .isEqualTo(
                        transaction.deviceId()
                );

        Transaction retrieved =
                transactionRepository
                        .findById(
                                transaction.id().value()
                        )
                        .orElseThrow();

        assertThat(retrieved)
                .isEqualTo(
                        transaction
                );
    }
}