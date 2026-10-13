package com.fraud_detector.shared.presentation.error;

import com.fraud_detector.transaction.application.IdempotencyKeyConflictException;
import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.presentation.controller.TransactionController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestExceptionHandlerTest {

    private final TransactionApplicationService transactionApplicationService =
            mock(TransactionApplicationService.class);

    private final RestExceptionHandler exceptionHandler =
            new RestExceptionHandler();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new TransactionController(
                            transactionApplicationService
                    )
            )
            .setControllerAdvice(exceptionHandler)
            .build();

    @Test
    void shouldReturnValidationErrorWhenRequestIsInvalid()
            throws Exception {
        String invalidJson = """
                {
                  "transaction": {
                    "userId": "",
                    "amount": {
                      "amount": 149.90,
                      "currency": ""
                    },
                    "merchant": "",
                    "category": "ELECTRONICS",
                    "timestamp": "2026-07-24T12:00:00Z",
                    "location": {
                      "country": "",
                      "state": "SP",
                      "city": "",
                      "latitude": -23.5505,
                      "longitude": -46.6333
                    },
                    "deviceId": ""
                  },
                  "context": {
                    "averageTransactionAmount": {
                      "amount": 100.00,
                      "currency": "BRL"
                    },
                    "usualStartTime": "08:00:00",
                    "usualEndTime": "22:00:00",
                    "knownDeviceIds": [],
                    "usualLocation": {
                      "country": "BR",
                      "state": "SP",
                      "city": "São Paulo",
                      "latitude": -23.5505,
                      "longitude": -46.6333
                    }
                  }
                }
                """;

        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .header(
                                        "Idempotency-Key",
                                        "rest-exception-test"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message").value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/transactions/analyze")
                )
                .andExpect(
                        jsonPath("$.fieldErrors[*].field").value(
                                hasItems(
                                        "transaction.userId",
                                        "transaction.amount.currency",
                                        "transaction.merchant",
                                        "transaction.location.country",
                                        "transaction.location.city",
                                        "transaction.deviceId",
                                        "context.knownDeviceIds"
                                )
                        )
                );
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsMissing()
            throws Exception {
        mockMvc.perform(
                        post("/api/v1/transactions/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message").value(
                                "Required request header is missing: Idempotency-Key"
                        )
                );
    }

    @Test
    void shouldReturnConflictWhenIdempotencyKeyIsReusedWithDifferentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/transactions/analyze"
        );

        var response = exceptionHandler.handleIdempotencyKeyConflict(
                new IdempotencyKeyConflictException(
                        "The idempotency key has already been used with a different request."
                ),
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(CONFLICT.value());

        assertThat(response.getBody()).isNotNull();
    }
}