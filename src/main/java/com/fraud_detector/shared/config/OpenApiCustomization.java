package com.fraud_detector.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiCustomization {

    @Bean
    public OpenAPI fraudDetectorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FraudDetector API")
                        .version("1.0.0")
                        .description(
                                "API for analyzing financial transactions and returning fraud risk assessments."
                        )
                        .contact(new Contact()
                                .name("FraudDetector Team"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}