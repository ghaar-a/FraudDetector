package com.fraud_detector.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldCreateValidTransaction() {
        Money amount = Money.of(150.00, "BRL");

        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                -23.5505,
                -46.6333
        );

        Instant timestamp = Instant.parse(
                "2026-07-22T15:30:00Z"
        );

        Transaction transaction = Transaction.create(
                "user-123",
                amount,
                "Electronics Store",
                TransactionCategory.ELECTRONICS,
                timestamp,
                location,
                "device-456"
        );

        assertNotNull(transaction.id());
        assertEquals("user-123", transaction.userId());
        assertEquals(amount, transaction.amount());
        assertEquals(
                "Electronics Store",
                transaction.merchant()
        );
        assertEquals(
                TransactionCategory.ELECTRONICS,
                transaction.category()
        );
        assertEquals(timestamp, transaction.timestamp());
        assertEquals(location, transaction.location());
        assertEquals("device-456", transaction.deviceId());
    }

    @Test
    void shouldGenerateUniqueTransactionIds() {
        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );

        Transaction first = Transaction.create(
                "user-123",
                Money.of(100.00, "BRL"),
                "Store",
                TransactionCategory.OTHER,
                Instant.now(),
                location,
                "device-123"
        );

        Transaction second = Transaction.create(
                "user-123",
                Money.of(100.00, "BRL"),
                "Store",
                TransactionCategory.OTHER,
                Instant.now(),
                location,
                "device-123"
        );

        assertNotEquals(
                first.id(),
                second.id()
        );
    }

    @Test
    void shouldRejectBlankUserId() {
        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.create(
                        " ",
                        Money.of(100.00, "BRL"),
                        "Store",
                        TransactionCategory.OTHER,
                        Instant.now(),
                        location,
                        "device-123"
                )
        );
    }

    @Test
    void shouldRejectBlankMerchant() {
        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.create(
                        "user-123",
                        Money.of(100.00, "BRL"),
                        " ",
                        TransactionCategory.OTHER,
                        Instant.now(),
                        location,
                        "device-123"
                )
        );
    }

    @Test
    void shouldRejectBlankDeviceId() {
        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.create(
                        "user-123",
                        Money.of(100.00, "BRL"),
                        "Store",
                        TransactionCategory.OTHER,
                        Instant.now(),
                        location,
                        " "
                )
        );
    }
}