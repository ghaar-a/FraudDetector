package com.fraud_detector.transaction.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TransactionAnalysisAcceptedResponse",
        description = "Response returned when a transaction analysis request is accepted for asynchronous processing."
)
public record TransactionAnalysisAcceptedResponse(

        @Schema(description = "Human-readable message.", example = "Transaction analysis request accepted.")
        String message,

        @Schema(description = "Status of the request.", example = "ACCEPTED")
        String status,

        @Schema(description = "Kafka topic that received the request.", example = "fraud.transaction.analysis.requested")
        String topic
) {
}