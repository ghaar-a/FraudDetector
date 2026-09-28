package com.fraud_detector.transaction.infrastructure.messaging.event;

import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TransactionAnalysisRequestedEventMapper {

    public TransactionAnalysisRequestedEvent toEvent(
            TransactionAnalysisRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Transaction analysis request cannot be null"
        );

        return new TransactionAnalysisRequestedEvent(
                toTransactionRequest(request.transaction()),
                toFraudRuleContextRequest(request.context())
        );
    }

    private TransactionAnalysisRequestedEvent.TransactionRequest toTransactionRequest(
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

    private TransactionAnalysisRequestedEvent.FraudRuleContextRequest toFraudRuleContextRequest(
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
}

