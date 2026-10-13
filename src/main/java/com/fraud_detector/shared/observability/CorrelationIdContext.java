package com.fraud_detector.shared.observability;

public final class CorrelationIdContext {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdContext() {
    }
}