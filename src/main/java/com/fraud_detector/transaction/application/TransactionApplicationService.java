package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TransactionApplicationService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public TransactionApplicationService(
            TransactionRepository transactionRepository,
            FraudDetectionService fraudDetectionService
    ) {
        this.transactionRepository = Objects.requireNonNull(
                transactionRepository,
                "Transaction repository cannot be null"
        );

        this.fraudDetectionService = Objects.requireNonNull(
                fraudDetectionService,
                "Fraud detection service cannot be null"
        );
    }

    @Transactional
    public FraudAnalysis process(
            Transaction transaction,
            FraudRuleContext context
    ) {
        Objects.requireNonNull(
                transaction,
                "Transaction cannot be null"
        );

        Objects.requireNonNull(
                context,
                "Fraud rule context cannot be null"
        );

        transactionRepository.save(
                transaction
        );

        return fraudDetectionService.analyze(
                transaction,
                context
        );
    }
}