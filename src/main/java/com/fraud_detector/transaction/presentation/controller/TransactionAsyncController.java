package com.fraud_detector.transaction.presentation.controller;

import com.fraud_detector.shared.presentation.error.ApiErrorResponse;
import com.fraud_detector.transaction.application.TransactionAnalysisAsyncService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisAcceptedResponse;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
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

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transaction analysis endpoints")
public class TransactionAsyncController {

    private final TransactionAnalysisAsyncService asyncService;
    private final TransactionAnalysisMapper mapper;

    public TransactionAsyncController(
            TransactionAnalysisAsyncService asyncService,
            TransactionAnalysisMapper mapper
    ) {
        this.asyncService = Objects.requireNonNull(
                asyncService,
                "Async service cannot be null"
        );
        this.mapper = Objects.requireNonNull(
                mapper,
                "Transaction analysis mapper cannot be null"
        );
    }

    @PostMapping("/analyze/async")
    @Operation(
            summary = "Submit a transaction for asynchronous analysis",
            description = "Publishes a transaction analysis request to Kafka and returns immediately."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Request accepted for asynchronous processing",
                    content = @Content(schema = @Schema(implementation = TransactionAnalysisAcceptedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TransactionAnalysisAcceptedResponse> analyzeAsync(
            @Valid @RequestBody TransactionAnalysisRequest request
    ) {
        TransactionAnalysisRequestedEvent event = mapper.toRequestedEvent(request);
        asyncService.requestAnalysis(event);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        new TransactionAnalysisAcceptedResponse(
                                "Transaction analysis request accepted.",
                                "ACCEPTED",
                                KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS
                        )
                );
    }
}