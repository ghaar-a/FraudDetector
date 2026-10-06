package com.fraud_detector.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicHealthEndpoint() throws Exception {
        mockMvc.perform(
                        get("/actuator/health")
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutCredentials()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidCredentials()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .with(
                                        httpBasic(
                                                "invalid-user",
                                                "invalid-password"
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowProtectedEndpointWithValidCredentials()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .with(
                                        httpBasic(
                                                "fraud-detector",
                                                "change-me"
                                        )
                                )
                )
                .andDo(print());
    }
}