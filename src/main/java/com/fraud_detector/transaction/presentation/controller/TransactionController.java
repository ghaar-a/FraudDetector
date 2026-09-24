package com.fraud_detector.transaction.presentation.controller;

import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.shared.presentation.error.ApiErrorResponse;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisAcceptedResponse;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(
        name = "Transactions",
        description = "Transaction analysis endpoints"
)
public class TransactionController {

    private final TransactionApplicationService transactionApplicationService;

    public TransactionController(
            TransactionApplicationService transactionApplicationService
    ) {
        this.transactionApplicationService = transactionApplicationService;
    }

    @PostMapping("/analyze")
    @Operation(
            summary = "Request transaction analysis",
            description = "Publishes a transaction analysis request for asynchronous fraud processing through Kafka."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Analysis request accepted for asynchronous processing",
                    content = @Content(
                            schema = @Schema(
                                    implementation = TransactionAnalysisAcceptedResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<TransactionAnalysisAcceptedResponse> analyze(
            @Valid @RequestBody TransactionAnalysisRequest request
    ) {
        Transaction transaction =
                toTransaction(request.transaction());

        FraudRuleContext context =
                toFraudRuleContext(request.context());

        transactionApplicationService.requestAnalysis(
                transaction,
                context
        );

        TransactionAnalysisAcceptedResponse response =
                new TransactionAnalysisAcceptedResponse(
                        "Transaction analysis request accepted.",
                        "ACCEPTED",
                        "fraud.transaction.analysis.requested"
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    private Transaction toTransaction(
            TransactionAnalysisRequest.TransactionRequest request
    ) {
        TransactionAnalysisRequest.LocationRequest location =
                request.location();

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
        TransactionAnalysisRequest.LocationRequest location =
                request.usualLocation();

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
}