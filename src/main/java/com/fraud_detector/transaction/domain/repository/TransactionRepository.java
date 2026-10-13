package com.fraud_detector.transaction.domain.repository;

import com.fraud_detector.transaction.domain.model.Transaction;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Transaction save(
            Transaction transaction,
            String idempotencyKey,
            String requestFingerprint
    );

    Optional<Transaction> findById(UUID transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<String> findFingerprintByIdempotencyKey(String idempotencyKey);
}
