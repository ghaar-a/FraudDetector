ALTER TABLE transactions
    ADD COLUMN idempotency_key VARCHAR(100),
    ADD COLUMN request_fingerprint CHAR(64);

CREATE UNIQUE INDEX uq_transactions_idempotency_key
    ON transactions (idempotency_key);

CREATE UNIQUE INDEX uq_fraud_analyses_transaction_id
    ON fraud_analyses (transaction_id);