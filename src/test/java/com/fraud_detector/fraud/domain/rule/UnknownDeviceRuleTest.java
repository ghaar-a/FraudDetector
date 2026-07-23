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

class UnknownDeviceRuleTest {

    private final UnknownDeviceRule rule =
            new UnknownDeviceRule();

    @Test
    void shouldDetectUnknownDevice() {
        Transaction transaction =
                createTransaction("unknown-device");

        FraudRuleContext context =
                createContext(
                        Set.of("known-device")
                );

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertTrue(result.suspicious());
        assertEquals(
                FraudReason.UNKNOWN_DEVICE,
                result.reason()
        );
    }

    @Test
    void shouldAcceptKnownDevice() {
        Transaction transaction =
                createTransaction("known-device");

        FraudRuleContext context =
                createContext(
                        Set.of("known-device")
                );

        FraudRuleResult result =
                rule.evaluate(
                        transaction,
                        context
                );

        assertFalse(result.suspicious());
    }

    private Transaction createTransaction(
            String deviceId
    ) {
        return Transaction.create(
                "user-123",
                Money.of(100.00, "BRL"),
                "Store",
                TransactionCategory.OTHER,
                Instant.parse(
                        "2026-07-22T15:00:00Z"
                ),
                createLocation(),
                deviceId
        );
    }

    private FraudRuleContext createContext(
            Set<String> knownDevices
    ) {
        return new FraudRuleContext(
                Money.of(
                        100.00,
                        "BRL"
                ),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                knownDevices,
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