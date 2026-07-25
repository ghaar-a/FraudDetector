CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              user_id VARCHAR(100) NOT NULL,
                              amount NUMERIC(19, 4) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              merchant_name VARCHAR(255) NOT NULL,
                              category VARCHAR(50) NOT NULL,
                              transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                              country VARCHAR(2) NOT NULL,
                              state VARCHAR(100),
                              city VARCHAR(150),
                              latitude NUMERIC(10, 7),
                              longitude NUMERIC(10, 7),
                              device_id VARCHAR(255) NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user_id
    ON transactions (user_id);

CREATE INDEX idx_transactions_transaction_timestamp
    ON transactions (transaction_timestamp);

CREATE INDEX idx_transactions_device_id
    ON transactions (device_id);

CREATE INDEX idx_transactions_user_timestamp
    ON transactions (user_id, transaction_timestamp);