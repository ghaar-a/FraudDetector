package com.fraud_detector.transaction.domain.model;

import java.util.Objects;

public record TransactionLocation(
        String country,
        String state,
        String city,
        Double latitude,
        Double longitude
) {

    public TransactionLocation {
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(city, "City cannot be null");

        if (country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be blank");
        }

        if (city.isBlank()) {
            throw new IllegalArgumentException("City cannot be blank");
        }

        validateCoordinates(latitude, longitude);

        country = country.toUpperCase();
    }

    private static void validateCoordinates(
            Double latitude,
            Double longitude
    ) {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }
    }
}