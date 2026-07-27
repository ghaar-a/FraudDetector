package com.fraud_detector.transaction.presentation.dto;

import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "FraudAnalysisResponse", description = "Result returned after the fraud analysis.")
public record FraudAnalysisResponse(

        @Schema(description = "Analysis identifier.", example = "2f9a5ab8-3de1-4b3f-bc6f-9f2d8f3f3c20")
        UUID analysisId,

        @Schema(description = "Transaction identifier.", example = "0d9c4d32-2f76-4f49-a2f2-1f8c5c5f0c12")
        UUID transactionId,

        @Schema(description = "Risk score between 0 and 1.", example = "0.75")
        BigDecimal riskScore,

        @Schema(description = "Computed risk level.", example = "HIGH")
        RiskLevel riskLevel,

        @Schema(description = "Decision returned by the fraud engine.", example = "REVIEW")
        FraudDecision decision,

        @Schema(description = "Triggered fraud reasons.")
        List<FraudReason> reasons,

        @Schema(description = "Analysis timestamp in UTC.", example = "2026-07-24T12:00:00Z")
        Instant analyzedAt
) {
}