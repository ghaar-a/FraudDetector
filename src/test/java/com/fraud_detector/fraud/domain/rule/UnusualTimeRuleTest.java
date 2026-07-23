package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UnusualTimeRuleTest {

    private final UnusualTimeRule rule =
            new UnusualTimeRule();

    @Test
    void shouldDetectTransactionOutsideUsualTime() {
        Transaction transaction =
                createTransaction(
                        "2026-07-22T03:00:00Z"
                );

        FraudRuleContext context =
                createContext(
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0)
                );

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertTrue(result.suspicious());
        assertEquals(
                FraudReason.UNUSUAL_TIME,
                result.reason()
        );
    }

    @Test
    void shouldAcceptTransactionWithinUsualTime() {
        Transaction transaction =
                createTransaction(
                        "2026-07-22T15:00:00Z"
                );

        FraudRuleContext context =
                createContext(
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0)
                );

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertFalse(result.suspicious());
    }

    @Test
    void shouldSupportPeriodCrossingMidnight() {
        Transaction transaction =
                createTransaction(
                        "2026-07-22T02:00:00Z"
                );

        FraudRuleContext context =
                createContext(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                );

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertFalse(result.suspicious());
    }

    private Transaction createTransaction(
            String timestamp
    ) {
        return Transaction.create(
                "user-123",
                Money.of(100.00, "BRL"),
                "Store",
                TransactionCategory.OTHER,
                Instant.parse(timestamp),
                createLocation(),
                "device-123"
        );
    }

    private FraudRuleContext createContext(
            LocalTime start,
            LocalTime end
    ) {
        return new FraudRuleContext(
                Money.of(
                        100.00,
                        "BRL"
                ),
                start,
                end,
                Set.of("device-123"),
                createLocation()
        );
    }

    private TransactionLocation createLocation() {
        return new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );
    }
}