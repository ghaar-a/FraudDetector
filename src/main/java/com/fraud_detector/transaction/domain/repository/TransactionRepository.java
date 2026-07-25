package com.fraud_detector.transaction.domain.repository;

import com.fraud_detector.transaction.domain.model.Transaction;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(
            Transaction transaction
    );

    Optional<Transaction> findById(
            UUID transactionId
    );
}