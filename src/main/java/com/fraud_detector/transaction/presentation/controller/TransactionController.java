
package com.fraud_detector.transaction.presentation.controller;

import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.presentation.dto.FraudAnalysisResponse;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionApplicationService transactionApplicationService;

    public TransactionController(
            TransactionApplicationService transactionApplicationService
    ) {
        this.transactionApplicationService = transactionApplicationService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<FraudAnalysisResponse> analyze(
            @Valid @RequestBody TransactionAnalysisRequest request
    ) {
        Transaction transaction = toTransaction(request.transaction());
        FraudRuleContext context = toFraudRuleContext(request.context());

        FraudAnalysis analysis = transactionApplicationService.process(
                transaction,
                context
        );

        return ResponseEntity.ok(toResponse(analysis));
    }

    private Transaction toTransaction(
            TransactionAnalysisRequest.TransactionRequest request
    ) {
        TransactionAnalysisRequest.LocationRequest location = request.location();

        return Transaction.create(
                request.userId(),
                Money.of(
                        request.amount().amount(),
                        request.amount().currency()
                ),
                request.merchant(),
                request.category(),
                request.timestamp(),
                new TransactionLocation(
                        location.country(),
                        location.state(),
                        location.city(),
                        location.latitude(),
                        location.longitude()
                ),
                request.deviceId()
        );
    }

    private FraudRuleContext toFraudRuleContext(
            TransactionAnalysisRequest.FraudRuleContextRequest request
    ) {
        TransactionAnalysisRequest.LocationRequest location = request.usualLocation();

        return new FraudRuleContext(
                Money.of(
                        request.averageTransactionAmount().amount(),
                        request.averageTransactionAmount().currency()
                ),
                request.usualStartTime(),
                request.usualEndTime(),
                request.knownDeviceIds(),
                new TransactionLocation(
                        location.country(),
                        location.state(),
                        location.city(),
                        location.latitude(),
                        location.longitude()
                )
        );
    }

    private FraudAnalysisResponse toResponse(
            FraudAnalysis analysis
    ) {
        return new FraudAnalysisResponse(
                analysis.id(),
                analysis.transactionId().value(),
                analysis.riskScore().value(),
                analysis.riskLevel(),
                analysis.decision(),
                analysis.reasons(),
                analysis.analyzedAt()
        );
    }
}
