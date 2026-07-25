CREATE TABLE fraud_analyses (
                                id UUID PRIMARY KEY,
                                transaction_id UUID NOT NULL,
                                risk_score NUMERIC(5, 4) NOT NULL,
                                risk_level VARCHAR(20) NOT NULL,
                                decision VARCHAR(20) NOT NULL,
                                analyzed_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                CONSTRAINT fk_fraud_analysis_transaction
                                    FOREIGN KEY (transaction_id)
                                        REFERENCES transactions (id)
);

CREATE INDEX idx_fraud_analyses_transaction_id
    ON fraud_analyses (transaction_id);

CREATE INDEX idx_fraud_analyses_decision
    ON fraud_analyses (decision);

CREATE INDEX idx_fraud_analyses_analyzed_at
    ON fraud_analyses (analyzed_at);