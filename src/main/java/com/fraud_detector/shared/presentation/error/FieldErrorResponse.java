package com.fraud_detector.shared.presentation.error;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}