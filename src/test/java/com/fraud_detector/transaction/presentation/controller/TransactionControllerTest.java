package com.fraud_detector.transaction.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.model.FraudDecision;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.model.RiskLevel;
import com.fraud_detector.fraud.domain.model.RiskScore;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.shared.presentation.error.RestExceptionHandler;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionId;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TransactionApplicationService transactionApplicationService;

    @BeforeEach
    void setUp() {
        this.transactionApplicationService =
                mock(TransactionApplicationService.class);

        this.objectMapper =
                new ObjectMapper().findAndRegisterModules();

        this.mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new TransactionController(
                                        transactionApplicationService
                                )
                        )
                        .setControllerAdvice(
                                new RestExceptionHandler()
                        )
                        .build();
    }

    @Test
    void shouldAnalyzeTransactionAndReturnAnalysis() throws Exception {
        FraudAnalysis analysis =
                FraudAnalysis.create(
                        TransactionId.generate(),
                        RiskScore.of(new BigDecimal("0.7500")),
                        RiskLevel.HIGH,
                        FraudDecision.REVIEW,
                        List.of(
                                FraudReason.UNUSUAL_AMOUNT,
                                FraudReason.UNKNOWN_DEVICE
                        )
                );

        when(
                transactionApplicationService.process(
                        any(Transaction.class),
                        any(FraudRuleContext.class)
                )
        ).thenReturn(analysis);

        TransactionAnalysisRequest request = createRequest();

        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.analysisId")
                                .value(analysis.id().toString())
                )
                .andExpect(
                        jsonPath("$.transactionId")
                                .value(
                                        analysis.transactionId()
                                                .value()
                                                .toString()
                                )
                )
                .andExpect(jsonPath("$.riskScore").value(0.75))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.decision").value("REVIEW"))
                .andExpect(
                        jsonPath("$.reasons[0]")
                                .value("UNUSUAL_AMOUNT")
                )
                .andExpect(
                        jsonPath("$.reasons[1]")
                                .value("UNKNOWN_DEVICE")
                );

        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        ArgumentCaptor<FraudRuleContext> contextCaptor =
                ArgumentCaptor.forClass(FraudRuleContext.class);

        verify(transactionApplicationService).process(
                transactionCaptor.capture(),
                contextCaptor.capture()
        );

        Transaction capturedTransaction =
                transactionCaptor.getValue();

        FraudRuleContext capturedContext =
                contextCaptor.getValue();

        assertThat(capturedTransaction).isNotNull();
        assertThat(capturedTransaction.userId())
                .isEqualTo("user-123");

        assertThat(capturedTransaction.amount())
                .isEqualTo(
                        Money.of(
                                new BigDecimal("149.90"),
                                "BRL"
                        )
                );

        assertThat(capturedTransaction.merchant())
                .isEqualTo("Electronics Store");

        assertThat(capturedTransaction.category())
                .isEqualTo(TransactionCategory.ELECTRONICS);

        assertThat(capturedTransaction.timestamp())
                .isEqualTo(
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        )
                );

        assertThat(capturedTransaction.location())
                .isEqualTo(
                        new TransactionLocation(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                );

        assertThat(capturedTransaction.deviceId())
                .isEqualTo("device-123");

        assertThat(capturedContext).isNotNull();

        assertThat(capturedContext.averageTransactionAmount())
                .isEqualTo(
                        Money.of(
                                new BigDecimal("100.00"),
                                "BRL"
                        )
                );

        assertThat(capturedContext.usualStartTime())
                .isEqualTo(LocalTime.of(8, 0));

        assertThat(capturedContext.usualEndTime())
                .isEqualTo(LocalTime.of(22, 0));

        assertThat(capturedContext.knownDeviceIds())
                .containsExactly("device-123");

        assertThat(capturedContext.usualLocation())
                .isEqualTo(
                        new TransactionLocation(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                );
    }

    @Test
    void shouldRejectInvalidTransactionRequest() throws Exception {
        String requestBody = """
                {
                  "transaction": {
                    "userId": "",
                    "amount": {
                      "amount": -10.00,
                      "currency": "brl"
                    },
                    "merchant": "",
                    "category": null,
                    "timestamp": null,
                    "location": {
                      "country": "Brazil",
                      "state": "SP",
                      "city": "",
                      "latitude": 120.0,
                      "longitude": -200.0
                    },
                    "deviceId": ""
                  },
                  "context": {
                    "averageTransactionAmount": {
                      "amount": 0,
                      "currency": "BRL"
                    },
                    "usualStartTime": null,
                    "usualEndTime": null,
                    "knownDeviceIds": [],
                    "usualLocation": null
                  }
                }
                """;

        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/transactions/analyze")
                )
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(
                        jsonPath("$.fieldErrors.length()")
                                .value(org.hamcrest.Matchers.greaterThan(0))
                );

        org.mockito.Mockito.verifyNoInteractions(
                transactionApplicationService
        );
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value("Malformed JSON request")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/transactions/analyze")
                );

        org.mockito.Mockito.verifyNoInteractions(
                transactionApplicationService
        );
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException()
            throws Exception {

        when(
                transactionApplicationService.process(
                        any(Transaction.class),
                        any(FraudRuleContext.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "Internal implementation detail"
                )
        );

        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest()
                                        )
                                )
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Unexpected error")
                );
    }

    private TransactionAnalysisRequest createRequest() {
        return new TransactionAnalysisRequest(
                new TransactionAnalysisRequest.TransactionRequest(
                        "user-123",
                        new TransactionAnalysisRequest.MoneyRequest(
                                new BigDecimal("149.90"),
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionAnalysisRequest.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-123"
                ),
                new TransactionAnalysisRequest.FraudRuleContextRequest(
                        new TransactionAnalysisRequest.MoneyRequest(
                                new BigDecimal("100.00"),
                                "BRL"
                        ),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        Set.of("device-123"),
                        new TransactionAnalysisRequest.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                )
        );
    }
}