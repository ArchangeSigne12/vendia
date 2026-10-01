

CREATE TABLE token_revoque (
    jti              VARCHAR(100) PRIMARY KEY,
    date_expiration  TIMESTAMP NOT NULL
);

CREATE INDEX idx_token_revoque_expiration ON token_revoque(date_expiration);
