package com.fraud_detector.fraud.domain.rule;

import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FraudRuleEngineTest {

    @Test
    void shouldReturnAllTriggeredFraudReasons() {
        FraudRuleEngine engine =
                new FraudRuleEngine(
                        List.of(
                                new UnusualAmountRule(),
                                new UnusualTimeRule(),
                                new UnknownDeviceRule()
                        )
                );

        Transaction transaction =
                Transaction.create(
                        "user-123",
                        Money.of(
                                1000.00,
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-22T03:00:00Z"
                        ),
                        createLocation(),
                        "unknown-device"
                );

        FraudRuleContext context =
                new FraudRuleContext(
                        Money.of(
                                100.00,
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("known-device"),
                        createLocation()
                );

        List<FraudReason> reasons =
                engine.evaluate(
                        transaction,
                        context
                );

        assertEquals(
                3,
                reasons.size()
        );

        assertTrue(
                reasons.contains(
                        FraudReason.UNUSUAL_AMOUNT
                )
        );

        assertTrue(
                reasons.contains(
                        FraudReason.UNUSUAL_TIME
                )
        );

        assertTrue(
                reasons.contains(
                        FraudReason.UNKNOWN_DEVICE
                )
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleIsTriggered() {
        FraudRuleEngine engine =
                new FraudRuleEngine(
                        List.of(
                                new UnusualAmountRule(),
                                new UnusualTimeRule(),
                                new UnknownDeviceRule()
                        )
                );

        Transaction transaction =
                Transaction.create(
                        "user-123",
                        Money.of(
                                100.00,
                                "BRL"
                        ),
                        "Grocery Store",
                        TransactionCategory.GROCERIES,
                        Instant.parse(
                                "2026-07-22T15:00:00Z"
                        ),
                        createLocation(),
                        "known-device"
                );

        FraudRuleContext context =
                new FraudRuleContext(
                        Money.of(
                                100.00,
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("known-device"),
                        createLocation()
                );

        List<FraudReason> reasons =
                engine.evaluate(
                        transaction,
                        context
                );

        assertTrue(reasons.isEmpty());
    }

    @Test
    void shouldRejectEmptyRuleList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FraudRuleEngine(
                        List.of()
                )
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