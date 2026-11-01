CREATE TABLE end_user (
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) UNIQUE,
    username       VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255),
    google_id      VARCHAR(255) UNIQUE,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    role           VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'MODERATOR', 'USER')),
    last_login_at  TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE auth (
    id         UUID PRIMARY KEY,
    user_id    UUID      NOT NULL REFERENCES end_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE user_tokens (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES end_user(id) ON DELETE CASCADE,
    token_type VARCHAR(20) NOT NULL CHECK (token_type IN ('FORGOT_PASSWORD', 'VALIDATE_USER')),
    hash_token VARCHAR(64) NOT NULL UNIQUE,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_tokens_user_type ON user_tokens(user_id, token_type);
