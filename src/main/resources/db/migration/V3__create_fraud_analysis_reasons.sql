CREATE TABLE fraud_analysis_reasons (
                                        analysis_id UUID NOT NULL,
                                        reason VARCHAR(50) NOT NULL,

                                        CONSTRAINT fk_fraud_analysis_reasons_analysis
                                            FOREIGN KEY (analysis_id)
                                                REFERENCES fraud_analyses (id)
                                                ON DELETE CASCADE
);

CREATE INDEX idx_fraud_analysis_reasons_analysis_id
    ON fraud_analysis_reasons (analysis_id);