ALTER TABLE transactions
ALTER COLUMN request_fingerprint
    TYPE VARCHAR(64)
    USING rtrim(request_fingerprint)::VARCHAR(64);