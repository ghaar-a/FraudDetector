package com.fraud_detector.transaction.presentation.dto;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

@Schema(
        name = "TransactionAnalysisRequest",
        description = "Request payload used to analyze a transaction and its fraud context."
)
public record TransactionAnalysisRequest(

        @Valid
        @NotNull
        @Schema(
                description = "Transaction data to be analyzed.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        TransactionRequest transaction,

        @Valid
        @NotNull
        @Schema(
                description = "Behavioral context used by the fraud rule engine.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        FraudRuleContextRequest context
) {

    @Schema(
            name = "TransactionRequest",
            description = "Transaction payload."
    )
    public record TransactionRequest(

            @NotBlank
            @Size(max = 100)
            @Schema(
                    description = "User identifier.",
                    example = "user-123",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String userId,

            @Valid
            @NotNull
            @Schema(
                    description = "Transaction amount.",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            MoneyRequest amount,

            @NotBlank
            @Size(max = 200)
            @Schema(
                    description = "Merchant name.",
                    example = "Electronics Store",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String merchant,

            @NotNull
            @Schema(
                    description = "Transaction category.",
                    example = "ELECTRONICS",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            TransactionCategory category,

            @NotNull
            @Schema(
                    description = "Transaction timestamp in UTC.",
                    example = "2026-07-24T12:00:00Z",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Instant timestamp,

            @Valid
            @NotNull
            @Schema(
                    description = "Transaction location.",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            LocationRequest location,

            @NotBlank
            @Size(max = 100)
            @Schema(
                    description = "Device identifier.",
                    example = "device-123",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String deviceId
    ) {
    }

    @Schema(
            name = "FraudRuleContextRequest",
            description = "Context used by the fraud rules."
    )
    public record FraudRuleContextRequest(

            @Valid
            @NotNull
            @Schema(
                    description = "Average transaction amount for the user.",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            MoneyRequest averageTransactionAmount,

            @NotNull
            @Schema(
                    description = "Usual start time of user activity.",
                    example = "08:00:00",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            LocalTime usualStartTime,

            @NotNull
            @Schema(
                    description = "Usual end time of user activity.",
                    example = "22:00:00",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            LocalTime usualEndTime,

            @NotEmpty
            @Size(max = 100)
            @Schema(
                    description = "Known device identifiers.",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Set<@NotBlank @Size(max = 100) String> knownDeviceIds,

            @Valid
            @NotNull
            @Schema(
                    description = "Usual location for the user.",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            LocationRequest usualLocation
    ) {
    }

    @Schema(
            name = "MoneyRequest",
            description = "Monetary value."
    )
    public record MoneyRequest(

            @NotNull
            @DecimalMin(
                    value = "0.01",
                    message = "Amount must be greater than zero"
            )
            @Digits(
                    integer = 15,
                    fraction = 4,
                    message = "Amount must have at most 15 integer digits and 4 decimal places"
            )
            @Schema(
                    description = "Amount value.",
                    example = "149.90",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            BigDecimal amount,

            @NotBlank
            @Pattern(
                    regexp = "^[A-Z]{3}$",
                    message = "Currency must be a three-letter uppercase ISO code"
            )
            @Schema(
                    description = "Currency code.",
                    example = "BRL",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String currency
    ) {
    }

    @Schema(
            name = "LocationRequest",
            description = "Geographic location."
    )
    public record LocationRequest(

            @NotBlank
            @Pattern(
                    regexp = "^[A-Z]{2}$",
                    message = "Country must be a two-letter uppercase country code"
            )
            @Schema(
                    description = "Country code.",
                    example = "BR",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String country,

            @Size(max = 100)
            @Schema(
                    description = "State or province.",
                    example = "SP"
            )
            String state,

            @NotBlank
            @Size(max = 150)
            @Schema(
                    description = "City.",
                    example = "São Paulo",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String city,

            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            @Schema(
                    description = "Latitude.",
                    example = "-23.5505"
            )
            Double latitude,

            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            @Schema(
                    description = "Longitude.",
                    example = "-46.6333"
            )
            Double longitude
    ) {
    }
}