package com.fraud_detector.transaction.presentation.dto;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

public record TransactionAnalysisRequest(
        @Valid
        @NotNull
        TransactionRequest transaction,

        @Valid
        @NotNull
        FraudRuleContextRequest context
) {

    public record TransactionRequest(
            @NotBlank
            String userId,

            @Valid
            @NotNull
            MoneyRequest amount,

            @NotBlank
            String merchant,

            @NotNull
            TransactionCategory category,

            @NotNull
            Instant timestamp,

            @Valid
            @NotNull
            LocationRequest location,

            @NotBlank
            String deviceId
    ) {
    }

    public record FraudRuleContextRequest(
            @Valid
            @NotNull
            MoneyRequest averageTransactionAmount,

            @NotNull
            LocalTime usualStartTime,

            @NotNull
            LocalTime usualEndTime,

            @NotEmpty
            Set<@NotBlank String> knownDeviceIds,

            @Valid
            @NotNull
            LocationRequest usualLocation
    ) {
    }

    public record MoneyRequest(
            @NotNull
            java.math.BigDecimal amount,

            @NotBlank
            String currency
    ) {
    }

    public record LocationRequest(
            @NotBlank
            String country,

            String state,

            @NotBlank
            String city,

            Double latitude,

            Double longitude
    ) {
    }
}