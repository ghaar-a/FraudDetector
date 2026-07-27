package com.fraud_detector.transaction.presentation.dto;

import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAnalysisResponse(
        UUID analysisId,
        UUID transactionId,
        BigDecimal riskScore,
        RiskLevel riskLevel,
        FraudDecision decision,
        List<FraudReason> reasons,
        Instant analyzedAt
) {
}