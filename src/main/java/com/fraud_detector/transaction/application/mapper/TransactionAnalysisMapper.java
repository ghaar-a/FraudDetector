package com.fraud_detector.transaction.application.mapper;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionAnalysisMapper {

    public TransactionAnalysisRequestedEvent toRequestedEvent(
            TransactionAnalysisRequest request
    ) {
        return new TransactionAnalysisRequestedEvent(
                toRequestedTransaction(request.transaction()),
                toRequestedContext(request.context())
        );
    }

    public Transaction toTransaction(
            TransactionAnalysisRequestedEvent.TransactionRequest request
    ) {
        return Transaction.create(
                request.userId(),
                toMoney(
                        request.amount().amount(),
                        request.amount().currency()
                ),
                request.merchant(),
                request.category(),
                request.timestamp(),
                toLocation(request.location()),
                request.deviceId()
        );
    }

    public FraudRuleContext toFraudRuleContext(
            TransactionAnalysisRequestedEvent.FraudRuleContextRequest request
    ) {
        return new FraudRuleContext(
                toMoney(
                        request.averageTransactionAmount().amount(),
                        request.averageTransactionAmount().currency()
                ),
                request.usualStartTime(),
                request.usualEndTime(),
                request.knownDeviceIds(),
                toLocation(request.usualLocation())
        );
    }

    private TransactionAnalysisRequestedEvent.TransactionRequest toRequestedTransaction(
            TransactionAnalysisRequest.TransactionRequest request
    ) {
        return new TransactionAnalysisRequestedEvent.TransactionRequest(
                request.userId(),
                new TransactionAnalysisRequestedEvent.MoneyRequest(
                        request.amount().amount(),
                        request.amount().currency()
                ),
                request.merchant(),
                request.category(),
                request.timestamp(),
                new TransactionAnalysisRequestedEvent.LocationRequest(
                        request.location().country(),
                        request.location().state(),
                        request.location().city(),
                        request.location().latitude(),
                        request.location().longitude()
                ),
                request.deviceId()
        );
    }

    private TransactionAnalysisRequestedEvent.FraudRuleContextRequest toRequestedContext(
            TransactionAnalysisRequest.FraudRuleContextRequest request
    ) {
        return new TransactionAnalysisRequestedEvent.FraudRuleContextRequest(
                new TransactionAnalysisRequestedEvent.MoneyRequest(
                        request.averageTransactionAmount().amount(),
                        request.averageTransactionAmount().currency()
                ),
                request.usualStartTime(),
                request.usualEndTime(),
                request.knownDeviceIds(),
                new TransactionAnalysisRequestedEvent.LocationRequest(
                        request.usualLocation().country(),
                        request.usualLocation().state(),
                        request.usualLocation().city(),
                        request.usualLocation().latitude(),
                        request.usualLocation().longitude()
                )
        );
    }

    private Money toMoney(
            BigDecimal amount,
            String currency
    ) {
        return Money.of(amount, currency);
    }

    private TransactionLocation toLocation(
            TransactionAnalysisRequestedEvent.LocationRequest location
    ) {
        return new TransactionLocation(
                location.country(),
                location.state(),
                location.city(),
                location.latitude(),
                location.longitude()
        );
    }
}