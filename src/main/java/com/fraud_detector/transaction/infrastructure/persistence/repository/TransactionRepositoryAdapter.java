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
    public Transaction save(
            Transaction transaction
    ) {
        TransactionEntity entity =
                TransactionEntity.fromDomain(
                        transaction
                );

        TransactionEntity saved =
                repository.save(
                        entity
                );

        return saved.toDomain();
    }

    @Override
    public Optional<Transaction> findById(
            UUID transactionId
    ) {
        return repository
                .findById(transactionId)
                .map(TransactionEntity::toDomain);
    }
}