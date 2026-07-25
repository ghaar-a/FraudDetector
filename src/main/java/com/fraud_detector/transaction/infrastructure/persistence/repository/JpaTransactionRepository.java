package com.fraud_detector.transaction.infrastructure.persistence.repository;

import com.fraud_detector.transaction.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaTransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {
}