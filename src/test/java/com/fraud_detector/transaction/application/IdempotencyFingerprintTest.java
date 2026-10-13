package com.fraud_detector.transaction.application;

import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyFingerprintTest {

    @Test
    void shouldGenerateSameFingerprintForEquivalentAmounts() {
        String firstFingerprint = IdempotencyFingerprint.calculate(
                createRequest(
                        new BigDecimal("149.90"),
                        "Electronics Store",
                        Set.of("device-123", "device-456")
                )
        );

        String secondFingerprint = IdempotencyFingerprint.calculate(
                createRequest(
                        new BigDecimal("149.9000"),
                        "Electronics Store",
                        Set.of("device-456", "device-123")
                )
        );

        assertThat(firstFingerprint)
                .isEqualTo(secondFingerprint);
    }

    @Test
    void shouldGenerateDifferentFingerprintWhenMerchantChanges() {
        String firstFingerprint = IdempotencyFingerprint.calculate(
                createRequest(
                        new BigDecimal("149.90"),
                        "Electronics Store",
                        Set.of("device-123")
                )
        );

        String secondFingerprint = IdempotencyFingerprint.calculate(
                createRequest(
                        new BigDecimal("149.90"),
                        "Different Store",
                        Set.of("device-123")
                )
        );

        assertThat(firstFingerprint)
                .isNotEqualTo(secondFingerprint);
    }

    @Test
    void shouldGenerateSha256HexadecimalFingerprint() {
        String fingerprint = IdempotencyFingerprint.calculate(
                createRequest(
                        new BigDecimal("149.90"),
                        "Electronics Store",
                        Set.of("device-123")
                )
        );

        assertThat(fingerprint)
                .hasSize(64)
                .matches("^[a-f0-9]{64}$");
    }

    @Test
    void shouldRejectNullRequest() {
        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> IdempotencyFingerprint.calculate(null)
        );
    }

    private TransactionAnalysisRequest createRequest(
            BigDecimal amount,
            String merchant,
            Set<String> knownDeviceIds
    ) {
        return new TransactionAnalysisRequest(
                new TransactionAnalysisRequest.TransactionRequest(
                        "user-123",
                        new TransactionAnalysisRequest.MoneyRequest(
                                amount,
                                "BRL"
                        ),
                        merchant,
                        TransactionCategory.ELECTRONICS,
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
                        knownDeviceIds,
                        new TransactionAnalysisRequest.LocationRequest(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                )
        );
    }
}