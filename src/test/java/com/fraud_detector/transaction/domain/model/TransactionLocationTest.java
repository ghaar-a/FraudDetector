package com.fraud_detector.transaction.domain.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLocationTest {

    @Test
    void shouldCreateValidLocation() {
        TransactionLocation location = new TransactionLocation(
                "br",
                "SP",
                "São Paulo",
                -23.5505,
                -46.6333
        );

        assertEquals("BR", location.country());
        assertEquals("SP", location.state());
        assertEquals("São Paulo", location.city());
        assertEquals(-23.5505, location.latitude());
        assertEquals(-46.6333, location.longitude());
    }

    @Test
    void shouldAllowLocationWithoutCoordinates() {
        TransactionLocation location = new TransactionLocation(
                "BR",
                "SP",
                "São Paulo",
                null,
                null
        );

        assertNotNull(location);
        assertNull(location.latitude());
        assertNull(location.longitude());
    }

    @Test
    void shouldRejectInvalidLatitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        91.0,
                        -46.6333
                )
        );
    }

    @Test
    void shouldRejectInvalidLongitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionLocation(
                        "BR",
                        "SP",
                        "São Paulo",
                        -23.5505,
                        181.0
                )
        );
    }

    @Test
    void shouldRejectBlankCountry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionLocation(
                        " ",
                        "SP",
                        "São Paulo",
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankCity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionLocation(
                        "BR",
                        "SP",
                        " ",
                        null,
                        null
                )
        );
    }
}