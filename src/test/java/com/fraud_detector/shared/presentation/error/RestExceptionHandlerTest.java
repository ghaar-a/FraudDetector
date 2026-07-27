package com.fraud_detector.shared.presentation.error;

import com.fraud_detector.transaction.application.TransactionApplicationService;
import com.fraud_detector.transaction.presentation.controller.TransactionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestExceptionHandlerTest {

    private final TransactionApplicationService transactionApplicationService =
            mock(TransactionApplicationService.class);

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TransactionController(transactionApplicationService))
            .setControllerAdvice(new RestExceptionHandler())
            .build();

    @Test
    void shouldReturnValidationErrorWhenRequestIsInvalid() throws Exception {
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/transactions/analyze"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(7)))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItems(
                        "transaction.userId",
                        "transaction.amount.currency",
                        "transaction.merchant",
                        "transaction.location.country",
                        "transaction.location.city",
                        "transaction.deviceId",
                        "context.knownDeviceIds"
                )));
    }
}