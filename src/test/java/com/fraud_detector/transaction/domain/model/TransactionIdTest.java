package com.fraud_detector.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTest {

    @Test
    void shouldGenerateTransactionId() {
        TransactionId transactionId = TransactionId.generate();

        assertNotNull(transactionId);
        assertNotNull(transactionId.value());
    }

    @Test
    void shouldCreateTransactionIdFromUuid() {
        UUID uuid = UUID.randomUUID();

        TransactionId transactionId = TransactionId.of(uuid);

        assertEquals(uuid, transactionId.value());
    }

    @Test
    void shouldCreateTransactionIdFromString() {
        UUID uuid = UUID.randomUUID();

        TransactionId transactionId = TransactionId.of(uuid.toString());

        assertEquals(uuid, transactionId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                NullPointerException.class,
                () -> new TransactionId(null)
        );
    }
}