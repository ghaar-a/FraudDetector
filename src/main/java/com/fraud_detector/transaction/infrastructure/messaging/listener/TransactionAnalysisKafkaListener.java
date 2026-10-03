package com.fraud_detector.transaction.infrastructure.messaging.listener;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransactionAnalysisKafkaListener {

    private final TransactionApplicationService transactionApplicationService;
    private final TransactionAnalysisMapper mapper;

    public TransactionAnalysisKafkaListener(
            TransactionApplicationService transactionApplicationService,
            TransactionAnalysisMapper mapper
    ) {
        this.transactionApplicationService = Objects.requireNonNull(
                transactionApplicationService,
                "Transaction application service cannot be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "Transaction analysis mapper cannot be null"
        );
    }

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "transactionAnalysisKafkaListenerContainerFactory"
    )
    public void onMessage(
            TransactionAnalysisRequestedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Event cannot be null"
        );

        Transaction transaction =
                mapper.toTransaction(
                        event.transaction()
                );

        FraudRuleContext context =
                mapper.toFraudRuleContext(
                        event.context()
                );

        transactionApplicationService.process(
                transaction,
                context
        );
    }
}