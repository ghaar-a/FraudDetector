package com.fraud_detector.transaction.infrastructure.messaging;

public final class KafkaTopics {

    public static final String TRANSACTION_ANALYSIS_REQUESTS =
            "fraud.transaction.analysis.requested";

    public static final String TRANSACTION_ANALYSIS_REQUESTS_DLT =
            TRANSACTION_ANALYSIS_REQUESTS + ".DLT";

    private KafkaTopics() {
    }
}