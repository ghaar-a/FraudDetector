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

class UnusualAmountRuleTest {

    private final UnusualAmountRule rule =
            new UnusualAmountRule();

    @Test
    void shouldDetectUnusualAmount() {
        Transaction transaction =
                createTransaction(600.00);

        FraudRuleContext context =
                createContext(100.00);

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertTrue(result.suspicious());
        assertEquals(
                FraudReason.UNUSUAL_AMOUNT,
                result.reason()
        );
    }

    @Test
    void shouldAcceptNormalAmount() {
        Transaction transaction =
                createTransaction(400.00);

        FraudRuleContext context =
                createContext(100.00);

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertFalse(result.suspicious());
    }

    private Transaction createTransaction(
            double amount
    ) {
        return Transaction.create(
                "user-123",
                Money.of(amount, "BRL"),
                "Store",
                TransactionCategory.ELECTRONICS,
                Instant.parse(
                        "2026-07-22T15:00:00Z"
                ),
                createLocation(),
                "device-123"
        );
    }

    private FraudRuleContext createContext(
            double averageAmount
    ) {
        return new FraudRuleContext(
                Money.of(
                        averageAmount,
                        "BRL"
                ),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
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