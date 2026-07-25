package com.fraud_detector.fraud.infrastructure.persistence.repository;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.fraud.infrastructure.persistence.entity.FraudAnalysisEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class FraudAnalysisRepositoryAdapter
        implements FraudAnalysisRepository {

    private final FraudAnalysisJpaRepository repository;

    public FraudAnalysisRepositoryAdapter(
            FraudAnalysisJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public FraudAnalysis save(
            FraudAnalysis analysis
    ) {
        FraudAnalysisEntity entity =
                FraudAnalysisEntity.fromDomain(
                        analysis
                );

        FraudAnalysisEntity saved =
                repository.save(
                        entity
                );

        return saved.toDomain();
    }

    @Override
    public Optional<FraudAnalysis> findById(
            UUID analysisId
    ) {
        return repository
                .findById(
                        analysisId
                )
                .map(
                        FraudAnalysisEntity::toDomain
                );
    }

    @Override
    public Optional<FraudAnalysis> findByTransactionId(
            UUID transactionId
    ) {
        return repository
                .findByTransactionId(
                        transactionId
                )
                .map(
                        FraudAnalysisEntity::toDomain
                );
    }
}