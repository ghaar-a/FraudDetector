package com.fraud_detector.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoney() {
        Money money = Money.of(
                new BigDecimal("100.00"),
                "BRL"
        );

        assertEquals(
                new BigDecimal("100.00"),
                money.amount()
        );

        assertEquals("BRL", money.currency());
    }

    @Test
    void shouldNormalizeCurrencyToUpperCase() {
        Money money = Money.of(
                new BigDecimal("100.00"),
                "brl"
        );

        assertEquals("BRL", money.currency());
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(
                        new BigDecimal("-1.00"),
                        "BRL"
                )
        );
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                NullPointerException.class,
                () -> Money.of(null, "BRL")
        );
    }

    @Test
    void shouldRejectBlankCurrency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(
                        new BigDecimal("100.00"),
                        " "
                )
        );
    }

    @Test
    void shouldDetermineWhenAmountIsGreaterThanAnotherAmount() {
        Money first = Money.of(200.00, "BRL");
        Money second = Money.of(100.00, "BRL");

        assertTrue(first.isGreaterThan(second));
    }

    @Test
    void shouldDetermineWhenAmountIsGreaterThanOrEqualToAnotherAmount() {
        Money first = Money.of(100.00, "BRL");
        Money second = Money.of(100.00, "BRL");

        assertTrue(first.isGreaterThanOrEqual(second));
    }

    @Test
    void shouldDetermineWhenAmountIsLessThanAnotherAmount() {
        Money first = Money.of(50.00, "BRL");
        Money second = Money.of(100.00, "BRL");

        assertTrue(first.isLessThan(second));
    }

    @Test
    void shouldRejectComparisonWithDifferentCurrencies() {
        Money brl = Money.of(100.00, "BRL");
        Money usd = Money.of(100.00, "USD");

        assertThrows(
                IllegalArgumentException.class,
                () -> brl.isGreaterThan(usd)
        );
    }
}