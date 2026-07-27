package com.fraud_detector.shared.presentation.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FieldErrorResponse", description = "Field-level validation error.")
public record FieldErrorResponse(

        @Schema(description = "Field name.", example = "transaction.userId")
        String field,

        @Schema(description = "Validation message.", example = "must not be blank")
        String message,

        @Schema(description = "Rejected value.")
        Object rejectedValue
) {
}