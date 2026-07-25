package com.fraud_detector.fraud.domain.repository;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;

import java.util.Optional;
import java.util.UUID;

public interface FraudAnalysisRepository {

    FraudAnalysis save(
            FraudAnalysis analysis
    );

    Optional<FraudAnalysis> findById(
            UUID analysisId
    );

    Optional<FraudAnalysis> findByTransactionId(
            UUID transactionId
    );
}