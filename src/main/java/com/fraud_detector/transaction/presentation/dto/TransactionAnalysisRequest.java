package com.fraud_detector.transaction.presentation.dto;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

@Schema(name = "TransactionAnalysisRequest", description = "Request payload used to analyze a transaction and its fraud context.")
public record TransactionAnalysisRequest(

        @Valid
        @NotNull
        @Schema(description = "Transaction data to be analyzed.", requiredMode = Schema.RequiredMode.REQUIRED)
        TransactionRequest transaction,

        @Valid
        @NotNull
        @Schema(description = "Behavioral context used by the fraud rule engine.", requiredMode = Schema.RequiredMode.REQUIRED)
        FraudRuleContextRequest context
) {

    @Schema(name = "TransactionRequest", description = "Transaction payload.")
    public record TransactionRequest(

            @NotBlank
            @Schema(description = "User identifier.", example = "user-123", requiredMode = Schema.RequiredMode.REQUIRED)
            String userId,

            @Valid
            @NotNull
            @Schema(description = "Transaction amount.", requiredMode = Schema.RequiredMode.REQUIRED)
            MoneyRequest amount,

            @NotBlank
            @Schema(description = "Merchant name.", example = "Electronics Store", requiredMode = Schema.RequiredMode.REQUIRED)
            String merchant,

            @NotNull
            @Schema(description = "Transaction category.", example = "ELECTRONICS", requiredMode = Schema.RequiredMode.REQUIRED)
            TransactionCategory category,

            @NotNull
            @Schema(description = "Transaction timestamp in UTC.", example = "2026-07-24T12:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            Instant timestamp,

            @Valid
            @NotNull
            @Schema(description = "Transaction location.", requiredMode = Schema.RequiredMode.REQUIRED)
            LocationRequest location,

            @NotBlank
            @Schema(description = "Device identifier.", example = "device-123", requiredMode = Schema.RequiredMode.REQUIRED)
            String deviceId
    ) {
    }

    @Schema(name = "FraudRuleContextRequest", description = "Context used by the fraud rules.")
    public record FraudRuleContextRequest(

            @Valid
            @NotNull
            @Schema(description = "Average transaction amount for the user.", requiredMode = Schema.RequiredMode.REQUIRED)
            MoneyRequest averageTransactionAmount,

            @NotNull
            @Schema(description = "Usual start time of user activity.", example = "08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime usualStartTime,

            @NotNull
            @Schema(description = "Usual end time of user activity.", example = "22:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime usualEndTime,

            @NotEmpty
            @Schema(description = "Known device identifiers.", requiredMode = Schema.RequiredMode.REQUIRED)
            Set<@NotBlank String> knownDeviceIds,

            @Valid
            @NotNull
            @Schema(description = "Usual location for the user.", requiredMode = Schema.RequiredMode.REQUIRED)
            LocationRequest usualLocation
    ) {
    }

    @Schema(name = "MoneyRequest", description = "Monetary value.")
    public record MoneyRequest(

            @NotNull
            @Schema(description = "Amount value.", example = "149.90", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal amount,

            @NotBlank
            @Schema(description = "Currency code.", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
            String currency
    ) {
    }

    @Schema(name = "LocationRequest", description = "Geographic location.")
    public record LocationRequest(

            @NotBlank
            @Schema(description = "Country code.", example = "BR", requiredMode = Schema.RequiredMode.REQUIRED)
            String country,

            @Schema(description = "State or province.", example = "SP")
            String state,

            @NotBlank
            @Schema(description = "City.", example = "São Paulo", requiredMode = Schema.RequiredMode.REQUIRED)
            String city,

            @Schema(description = "Latitude.", example = "-23.5505")
            Double latitude,

            @Schema(description = "Longitude.", example = "-46.6333")
            Double longitude
    ) {
    }
}