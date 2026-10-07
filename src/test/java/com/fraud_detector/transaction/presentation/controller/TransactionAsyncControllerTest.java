package com.fraud_detector.transaction.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.shared.presentation.error.RestExceptionHandler;
import com.fraud_detector.transaction.application.TransactionAnalysisAsyncService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.infrastructure.messaging.KafkaTopics;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionAsyncControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TransactionAnalysisAsyncService asyncService;
    private TransactionAnalysisMapper mapper;

    @BeforeEach
    void setUp() {
        this.asyncService =
                mock(TransactionAnalysisAsyncService.class);

        this.mapper =
                new TransactionAnalysisMapper();

        this.objectMapper =
                new ObjectMapper().findAndRegisterModules();

        this.mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new TransactionAsyncController(
                                        asyncService,
                                        mapper
                                )
                        )
                        .setControllerAdvice(
                                new RestExceptionHandler()
                        )
                        .build();
    }

    @Test
    void shouldAcceptTransactionAnalysisRequest()
            throws Exception {

        TransactionAnalysisRequest request =
                createRequest();

        mockMvc.perform(
                        post("/api/v1/transactions/analyze/async")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isAccepted())
                .andExpect(
                        jsonPath("$.status")
                                .value("ACCEPTED")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Transaction analysis request accepted."
                                )
                )
                .andExpect(
                        jsonPath("$.topic")
                                .value(
                                        KafkaTopics.TRANSACTION_ANALYSIS_REQUESTS
                                )
                );

        ArgumentCaptor<TransactionAnalysisRequestedEvent> captor =
                ArgumentCaptor.forClass(
                        TransactionAnalysisRequestedEvent.class
                );

        verify(asyncService)
                .requestAnalysis(captor.capture());

        TransactionAnalysisRequestedEvent capturedEvent =
                captor.getValue();

        assertThat(capturedEvent).isNotNull();

        assertThat(
                capturedEvent.transaction().userId()
        ).isEqualTo("user-123");

        assertThat(
                capturedEvent.context().knownDeviceIds()
        ).containsExactly("device-123");
    }

    @Test
    void shouldRejectInvalidTransactionAnalysisRequest()
            throws Exception {

        String requestBody = """
                {
                  "transaction": {
                    "userId": "",
                    "amount": {
                      "amount": -1,
                      "currency": "brl"
                    },
                    "merchant": "",
                    "category": null,
                    "timestamp": null,
                    "location": null,
                    "deviceId": ""
                  },
                  "context": null
                }
                """;

        mockMvc.perform(
                        post("/api/v1/transactions/analyze/async")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/transactions/analyze/async"
                                )
                )
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verifyNoInteractions(asyncService);
    }

    @Test
    void shouldRejectMalformedJson()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/transactions/analyze/async")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Malformed JSON request")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/transactions/analyze/async"
                                )
                );

        verifyNoInteractions(asyncService);
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