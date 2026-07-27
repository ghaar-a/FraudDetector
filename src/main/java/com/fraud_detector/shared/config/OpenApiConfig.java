package com.fraud_detector.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FraudDetector API",
                version = "1.0.0",
                description = "API for analyzing financial transactions and returning fraud risk assessments.",
                contact = @Contact(name = "FraudDetector Team")
        ),
        tags = {
                @Tag(name = "Transactions", description = "Transaction analysis endpoints")
        }
)
public class OpenApiConfig {
}