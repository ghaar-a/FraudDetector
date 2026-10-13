package com.fraud_detector.transaction.infrastructure.persistence.repository;

import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import com.fraud_detector.transaction.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter
        implements TransactionRepository {

    private final JpaTransactionRepository repository;

    public TransactionRepositoryAdapter(
            JpaTransactionRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return saveEntity(TransactionEntity.fromDomain(transaction));
    }

    @Override
    public Transaction save(
            Transaction transaction,
            String idempotencyKey,
            String requestFingerprint
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot be blank"
            );
        }

        if (requestFingerprint == null
                || !requestFingerprint.matches("^[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException(
                    "Request fingerprint must be a SHA-256 hexadecimal value"
            );
        }

        TransactionEntity entity = TransactionEntity.fromDomain(
                transaction,
                idempotencyKey,
                requestFingerprint
        );

        return saveEntity(entity);
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return repository.findById(transactionId)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(
            String idempotencyKey
    ) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Optional<String> findFingerprintByIdempotencyKey(
            String idempotencyKey
    ) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(TransactionEntity::getRequestFingerprint);
    }

    private Transaction saveEntity(TransactionEntity entity) {
        TransactionEntity saved = repository.save(entity);
        return saved.toDomain();
    }
}
