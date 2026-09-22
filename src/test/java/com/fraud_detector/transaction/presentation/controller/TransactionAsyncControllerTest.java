package com.fraud_detector.transaction.presentation.controller;

import com.fraud_detector.transaction.application.TransactionAnalysisAsyncService;
import com.fraud_detector.transaction.application.mapper.TransactionAnalysisMapper;
import com.fraud_detector.transaction.infrastructure.messaging.event.TransactionAnalysisRequestedEvent;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        this.asyncService = mock(TransactionAnalysisAsyncService.class);
        this.mapper = new TransactionAnalysisMapper();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionAsyncController(asyncService, mapper))
                .build();
    }

    @Test
    void shouldAcceptTransactionAnalysisRequest() throws Exception {
        TransactionAnalysisRequest request =
                new TransactionAnalysisRequest(
                        new TransactionAnalysisRequest.TransactionRequest(
                                "user-123",
                                new TransactionAnalysisRequest.MoneyRequest(
                                        new BigDecimal("149.90"),
                                        "BRL"
                                ),
                                "Electronics Store",
                                com.fraud_detector.transaction.domain.model.TransactionCategory.ELECTRONICS,
                                Instant.parse("2026-07-24T12:00:00Z"),
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

        mockMvc.perform(
                        post("/api/v1/transactions/analyze/async")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.message").value("Transaction analysis request accepted."))
                .andExpect(jsonPath("$.topic").value("fraud.transaction.analysis.requested"));

        ArgumentCaptor<TransactionAnalysisRequestedEvent> captor =
                ArgumentCaptor.forClass(TransactionAnalysisRequestedEvent.class);

        verify(asyncService).requestAnalysis(captor.capture());

        TransactionAnalysisRequestedEvent capturedEvent = captor.getValue();
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.transaction().userId()).isEqualTo("user-123");
        assertThat(capturedEvent.context().knownDeviceIds()).containsExactly("device-123");
    }
}