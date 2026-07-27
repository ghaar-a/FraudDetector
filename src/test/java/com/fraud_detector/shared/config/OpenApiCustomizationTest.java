package com.fraud_detector.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiCustomizationTest {

    @Test
    void shouldCreateOpenApiMetadata() {
        OpenAPI openAPI = new OpenApiCustomization().fraudDetectorOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("FraudDetector API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals(
                "API for analyzing financial transactions and returning fraud risk assessments.",
                openAPI.getInfo().getDescription()
        );
        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
        assertEquals("http://localhost:8080", openAPI.getServers().get(0).getUrl());
    }
}