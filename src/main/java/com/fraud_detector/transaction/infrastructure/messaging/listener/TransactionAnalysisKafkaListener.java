package com.fraud_detector.transaction.infrastructure.messaging.listener;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.shared.observability.CorrelationIdContext;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.TransactionAnalysisMetrics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransactionAnalysisKafkaListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionAnalysisKafkaListener.class);

    private final TransactionApplicationService transactionApplicationService;
    private final TransactionAnalysisMapper mapper;
    private final TransactionAnalysisMetrics metrics;

    public TransactionAnalysisKafkaListener(
            TransactionApplicationService transactionApplicationService,
            TransactionAnalysisMapper mapper,
            TransactionAnalysisMetrics metrics
    ) {
        this.transactionApplicationService = Objects.requireNonNull(
                transactionApplicationService,
                "Transaction application service cannot be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "Transaction analysis mapper cannot be null"
        );

        this.metrics = Objects.requireNonNull(
                metrics,
                "Transaction analysis metrics cannot be null"
        );
    }

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "transactionAnalysisKafkaListenerContainerFactory"
    )
    public void consume(
            TransactionAnalysisRequestedEvent event,
            @Header(
                    name = CorrelationIdContext.HEADER_NAME,
                    required = false
            ) String correlationId
    ) {
        processWithCorrelationId(event, correlationId);
    }

    public void onMessage(TransactionAnalysisRequestedEvent event) {
        processWithCorrelationId(event, null);
    }

    private void processWithCorrelationId(
            TransactionAnalysisRequestedEvent event,
            String correlationId
    ) {
        Objects.requireNonNull(event, "Event cannot be null");

        String previousCorrelationId =
                MDC.get(CorrelationIdContext.MDC_KEY);

        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CorrelationIdContext.MDC_KEY, correlationId);
        }

        long startNanos = System.nanoTime();
        metrics.recordProcessingStarted();

        try {
            LOGGER.info("Starting transaction analysis event processing");

            validateIdempotencyMetadata(event);

            Transaction transaction = mapper.toTransaction(
                    event.transaction()
            );

            FraudRuleContext context = mapper.toFraudRuleContext(
                    event.context()
            );

            if (event.idempotencyKey() != null) {
                transactionApplicationService.process(
                        transaction,
                        context,
                        event.idempotencyKey(),
                        event.requestFingerprint()
                );
            } else {
                transactionApplicationService.process(
                        transaction,
                        context
                );
            }

            metrics.recordProcessingSuccess();
            LOGGER.info("Transaction analysis event processed successfully");
        } catch (RuntimeException exception) {
            metrics.recordProcessingFailure();
            LOGGER.error(
                    "Transaction analysis event processing failed",
                    exception
            );
            throw exception;
        } finally {
            metrics.recordProcessingDuration(
                    System.nanoTime() - startNanos
            );

            if (previousCorrelationId == null) {
                MDC.remove(CorrelationIdContext.MDC_KEY);
            } else {
                MDC.put(
                        CorrelationIdContext.MDC_KEY,
                        previousCorrelationId
                );
            }
        }
    }

    private void validateIdempotencyMetadata(
            TransactionAnalysisRequestedEvent event
    ) {
        boolean hasIdempotencyKey = event.idempotencyKey() != null;
        boolean hasRequestFingerprint = event.requestFingerprint() != null;

        if (hasIdempotencyKey != hasRequestFingerprint) {
            throw new IllegalArgumentException(
                    "Idempotency key and request fingerprint must either "
                            + "both be present or both be absent."
            );
        }
    }
}
