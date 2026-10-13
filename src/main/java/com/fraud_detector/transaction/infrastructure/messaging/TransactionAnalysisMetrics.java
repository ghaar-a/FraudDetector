package com.fraud_detector.transaction.infrastructure.messaging;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionAnalysisMetrics {

    private static final String METRIC_PREFIX =
            "fraud_detector.kafka.transaction_analysis";

    private static final String PUBLISH_METRIC =
            METRIC_PREFIX + ".publish";

    private static final String PROCESSING_METRIC =
            METRIC_PREFIX + ".processing";

    private static final String PROCESSING_DURATION_METRIC =
            PROCESSING_METRIC + ".duration";

    private final MeterRegistry meterRegistry;

    public TransactionAnalysisMetrics(
            MeterRegistry meterRegistry
    ) {
        this.meterRegistry = Objects.requireNonNull(
                meterRegistry,
                "Meter registry cannot be null"
        );

        registerCounter(PUBLISH_METRIC, "success");
        registerCounter(PUBLISH_METRIC, "failure");

        registerCounter(PROCESSING_METRIC, "started");
        registerCounter(PROCESSING_METRIC, "success");
        registerCounter(PROCESSING_METRIC, "failure");
    }

    public void recordPublishSuccess() {
        incrementCounter(PUBLISH_METRIC, "success");
    }

    public void recordPublishFailure() {
        incrementCounter(PUBLISH_METRIC, "failure");
    }

    public void recordProcessingStarted() {
        incrementCounter(PROCESSING_METRIC, "started");
    }

    public void recordProcessingSuccess() {
        incrementCounter(PROCESSING_METRIC, "success");
    }

    public void recordProcessingFailure() {
        incrementCounter(PROCESSING_METRIC, "failure");
    }

    public void recordProcessingDuration(
            long durationNanos
    ) {
        meterRegistry.timer(
                PROCESSING_DURATION_METRIC
        ).record(
                durationNanos,
                TimeUnit.NANOSECONDS
        );
    }

    private void registerCounter(
            String metricName,
            String result
    ) {
        meterRegistry.counter(
                metricName,
                "result",
                result
        );
    }

    private void incrementCounter(
            String metricName,
            String result
    ) {
        meterRegistry.counter(
                metricName,
                "result",
                result
        ).increment();
    }
}
