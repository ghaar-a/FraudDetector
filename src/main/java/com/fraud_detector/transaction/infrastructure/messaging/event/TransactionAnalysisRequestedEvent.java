package com.fraud_detector.transaction.infrastructure.messaging.event;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

public record TransactionAnalysisRequestedEvent(
        TransactionRequest transaction,
        FraudRuleContextRequest context
) {

    public static TransactionAnalysisRequestedEvent from(
            Transaction transaction,
            FraudRuleContext context
    ) {
        return new TransactionAnalysisRequestedEvent(
                TransactionRequest.from(transaction),
                FraudRuleContextRequest.from(context)
        );
    }

    public record TransactionRequest(
            String userId,
            MoneyRequest amount,
            String merchant,
            TransactionCategory category,
            Instant timestamp,
            LocationRequest location,
            String deviceId
    ) {

        private static TransactionRequest from(
                Transaction transaction
        ) {
            return new TransactionRequest(
                    transaction.userId(),
                    MoneyRequest.from(transaction.amount()),
                    transaction.merchant(),
                    transaction.category(),
                    transaction.timestamp(),
                    LocationRequest.from(transaction.location()),
                    transaction.deviceId()
            );
        }
    }

    public record FraudRuleContextRequest(
            MoneyRequest averageTransactionAmount,
            LocalTime usualStartTime,
            LocalTime usualEndTime,
            Set<String> knownDeviceIds,
            LocationRequest usualLocation
    ) {

        private static FraudRuleContextRequest from(
                FraudRuleContext context
        ) {
            return new FraudRuleContextRequest(
                    MoneyRequest.from(
                            context.averageTransactionAmount()
                    ),
                    context.usualStartTime(),
                    context.usualEndTime(),
                    context.knownDeviceIds(),
                    LocationRequest.from(
                            context.usualLocation()
                    )
            );
        }
    }

    public record MoneyRequest(
            BigDecimal amount,
            String currency
    ) {

        private static MoneyRequest from(
                Money money
        ) {
            return new MoneyRequest(
                    money.amount(),
                    money.currency()
            );
        }
    }

    public record LocationRequest(
            String country,
            String state,
            String city,
            Double latitude,
            Double longitude
    ) {

        private static LocationRequest from(
                TransactionLocation location
        ) {
            return new LocationRequest(
                    location.country(),
                    location.state(),
                    location.city(),
                    location.latitude(),
                    location.longitude()
            );
        }
    }
}