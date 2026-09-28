package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.infrastructure.messaging.producer.TransactionAnalysisKafkaProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TransactionApplicationService {

    private final TransactionRepository transactionRepository;
    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionAnalysisKafkaProducer transactionAnalysisKafkaProducer;

    public TransactionApplicationService(
            TransactionRepository transactionRepository,
            FraudAnalysisRepository fraudAnalysisRepository,
            FraudDetectionService fraudDetectionService,
            TransactionAnalysisKafkaProducer transactionAnalysisKafkaProducer
    ) {
        this.transactionRepository = Objects.requireNonNull(
                transactionRepository,
                "Transaction repository cannot be null"
        );
        this.fraudAnalysisRepository = Objects.requireNonNull(
                fraudAnalysisRepository,
                "Fraud analysis repository cannot be null"
        );
        this.fraudDetectionService = Objects.requireNonNull(
                fraudDetectionService,
                "Fraud detection service cannot be null"
        );
        this.transactionAnalysisKafkaProducer = Objects.requireNonNull(
                transactionAnalysisKafkaProducer,
                "Transaction analysis Kafka producer cannot be null"
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

        transactionRepository.save(transaction);

        FraudAnalysis analysis =
                fraudDetectionService.analyze(transaction, context);

        return fraudAnalysisRepository.save(analysis);
    }

    public void requestAsyncAnalysis(
            TransactionAnalysisRequestedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Transaction analysis event cannot be null"
        );

        transactionAnalysisKafkaProducer.publish(event);
    }
}
