package com.fraud_detector.transaction.application;

import com.fraud_detector.transaction.presentation.dto.TransactionAnalysisRequest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public final class IdempotencyFingerprint {

    private IdempotencyFingerprint() {
    }

    public static String calculate(TransactionAnalysisRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            TransactionAnalysisRequest.TransactionRequest transaction =
                    request.transaction();

            TransactionAnalysisRequest.FraudRuleContextRequest context =
                    request.context();

            update(digest, transaction.userId());
            update(digest, transaction.amount().amount().stripTrailingZeros().toPlainString());
            update(digest, transaction.amount().currency());
            update(digest, transaction.merchant());
            update(digest, transaction.category().name());
            update(digest, transaction.timestamp().toString());
            update(digest, transaction.deviceId());

            updateLocation(digest, transaction.location());

            update(
                    digest,
                    context.averageTransactionAmount()
                            .amount()
                            .stripTrailingZeros()
                            .toPlainString()
            );
            update(digest, context.averageTransactionAmount().currency());
            update(digest, context.usualStartTime().toString());
            update(digest, context.usualEndTime().toString());

            List<String> sortedDeviceIds =
                    context.knownDeviceIds()
                            .stream()
                            .sorted()
                            .toList();

            update(digest, Integer.toString(sortedDeviceIds.size()));

            for (String deviceId : sortedDeviceIds) {
                update(digest, deviceId);
            }

            updateLocation(digest, context.usualLocation());

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    private static void updateLocation(
            MessageDigest digest,
            TransactionAnalysisRequest.LocationRequest location
    ) {
        update(digest, location.country());
        update(digest, location.state());
        update(digest, location.city());
        update(
                digest,
                location.latitude() == null
                        ? null
                        : Double.toString(location.latitude())
        );
        update(
                digest,
                location.longitude() == null
                        ? null
                        : Double.toString(location.longitude())
        );
    }

    private static void update(
            MessageDigest digest,
            String value
    ) {
        if (value == null) {
            digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
            return;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        digest.update(
                ByteBuffer.allocate(Integer.BYTES)
                        .putInt(bytes.length)
                        .array()
        );
        digest.update(bytes);
    }
}