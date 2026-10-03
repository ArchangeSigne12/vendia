CREATE TABLE refresh_token_revoque (
    token_hash      VARCHAR(64) PRIMARY KEY,
    date_revocation TIMESTAMP   NOT NULL DEFAULT now()
);