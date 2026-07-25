package com.fraud_detector.fraud.infrastructure.persistence.entity;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.transaction.domain.model.TransactionId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fraud_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FraudAnalysisEntity {

    @Id
    private UUID id;

    @Column(
            name = "transaction_id",
            nullable = false
    )
    private UUID transactionId;

    @Column(
            name = "risk_score",
            nullable = false,
            precision = 5,
            scale = 4
    )
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "risk_level",
            nullable = false,
            length = 20
    )
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "decision",
            nullable = false,
            length = 20
    )
    private FraudDecision decision;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "fraud_analysis_reasons",
            joinColumns = @JoinColumn(
                    name = "analysis_id"
            )
    )
    @Column(
            name = "reason",
            nullable = false,
            length = 50
    )
    @Enumerated(EnumType.STRING)
    private List<FraudReason> reasons =
            new ArrayList<>();

    @Column(
            name = "analyzed_at",
            nullable = false
    )
    private Instant analyzedAt;

    private FraudAnalysisEntity(
            UUID id,
            UUID transactionId,
            BigDecimal riskScore,
            RiskLevel riskLevel,
            FraudDecision decision,
            List<FraudReason> reasons,
            Instant analyzedAt
    ) {
        this.id = id;
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.reasons = new ArrayList<>(
                reasons
        );
        this.analyzedAt = analyzedAt;
    }

    public static FraudAnalysisEntity fromDomain(
            FraudAnalysis analysis
    ) {
        return new FraudAnalysisEntity(
                analysis.id(),
                analysis.transactionId().value(),
                analysis.riskScore().value(),
                analysis.riskLevel(),
                analysis.decision(),
                analysis.reasons(),
                analysis.analyzedAt()
        );
    }

    public FraudAnalysis toDomain() {
        return new FraudAnalysis(
                id,
                TransactionId.of(
                        transactionId
                ),
                RiskScore.of(
                        riskScore
                ),
                riskLevel,
                decision,
                List.copyOf(
                        reasons
                ),
                analyzedAt
        );
    }
}