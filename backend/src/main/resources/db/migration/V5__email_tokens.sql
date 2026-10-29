CREATE TABLE user_tokens(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL,
    token_type VARCHAR(20) NOT NULL, 
    hash_token VARCHAR(64) UNIQUE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE, 
    expired_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_user_tokens_type CHECK (token_type IN ('FORGOT_PASSWORD', 'VALIDATE_USER')),
    CONSTRAINT fk_user_tokens 
        FOREIGN KEY(user_id) REFERENCES end_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_tokens_user_type ON user_tokens(user_id, token_type);
