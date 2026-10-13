package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.repository.FraudAnalysisRepository;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionApplicationService {

    private final TransactionRepository transactionRepository;
    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final TransactionAnalysisProcessor processor;

    public TransactionApplicationService(
            TransactionRepository transactionRepository,
            FraudAnalysisRepository fraudAnalysisRepository,
            TransactionAnalysisProcessor processor
    ) {
        this.transactionRepository = Objects.requireNonNull(
                transactionRepository,
                "Transaction repository cannot be null"
        );
        this.fraudAnalysisRepository = Objects.requireNonNull(
                fraudAnalysisRepository,
                "Fraud analysis repository cannot be null"
        );
        this.processor = Objects.requireNonNull(
                processor,
                "Transaction analysis processor cannot be null"
        );
    }

    public FraudAnalysis process(
            Transaction transaction,
            FraudRuleContext context
    ) {
        return processor.process(transaction, context);
    }

    public FraudAnalysis process(
            Transaction transaction,
            FraudRuleContext context,
            String idempotencyKey,
            String requestFingerprint
    ) {
        Objects.requireNonNull(
                transaction,
                "Transaction cannot be null"
        );
        Objects.requireNonNull(
                context,
                "Fraud rule context cannot be null"
        );
        validateIdempotencyKey(idempotencyKey);
        validateFingerprint(requestFingerprint);

        Optional<FraudAnalysis> existingAnalysis = findExistingAnalysis(
                idempotencyKey,
                requestFingerprint
        );

        if (existingAnalysis.isPresent()) {
            return existingAnalysis.get();
        }

        try {
            return processor.process(
                    transaction,
                    context,
                    idempotencyKey,
                    requestFingerprint
            );
        } catch (DataIntegrityViolationException exception) {
            Optional<FraudAnalysis> concurrentAnalysis =
                    findExistingAnalysis(
                            idempotencyKey,
                            requestFingerprint
                    );

            if (concurrentAnalysis.isPresent()) {
                return concurrentAnalysis.get();
            }

            throw exception;
        }
    }

    private Optional<FraudAnalysis> findExistingAnalysis(
            String idempotencyKey,
            String requestFingerprint
    ) {
        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (existingTransaction.isEmpty()) {
            return Optional.empty();
        }

        String existingFingerprint =
                transactionRepository
                        .findFingerprintByIdempotencyKey(idempotencyKey)
                        .orElse(null);

        if (!Objects.equals(
                existingFingerprint,
                requestFingerprint
        )) {
            throw new IdempotencyKeyConflictException(
                    "The idempotency key has already been used with a different request."
            );
        }

        return fraudAnalysisRepository.findByTransactionId(
                existingTransaction.get().id().value()
        );
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must contain between 1 and 100 characters."
            );
        }
    }

    private void validateFingerprint(String requestFingerprint) {
        if (requestFingerprint == null
                || !requestFingerprint.matches("^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException(
                    "Request fingerprint must be a SHA-256 hexadecimal value."
            );
        }
    }
}
