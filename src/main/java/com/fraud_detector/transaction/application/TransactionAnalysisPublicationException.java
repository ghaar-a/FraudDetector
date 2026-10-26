
package com.fraud_detector.transaction.application;

public class TransactionAnalysisPublicationException extends RuntimeException {

    public TransactionAnalysisPublicationException(Throwable cause) {
        super("Transaction analysis request could not be published", cause);
    }
}
