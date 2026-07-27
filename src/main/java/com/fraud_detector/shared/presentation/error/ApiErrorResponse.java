package com.fraud_detector.shared.presentation.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "ApiErrorResponse", description = "Standard API error response.")
public record ApiErrorResponse(

        @Schema(description = "Error timestamp.", example = "2026-07-24T12:00:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code.", example = "400")
        int status,

        @Schema(description = "HTTP reason phrase.", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error message.", example = "Validation failed")
        String message,

        @Schema(description = "Request path.", example = "/api/v1/transactions/analyze")
        String path,

        @Schema(description = "Field-level validation errors.")
        List<FieldErrorResponse> fieldErrors
) {
}