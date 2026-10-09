CREATE TABLE auth ( 
    id UUID PRIMARY KEY, 
    user_id UUID NOT NULL,
    token_hash VARCHAR(255),
    expires_at TIMESTAMP NOT NULL, 
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE, 
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_auth_user
        FOREIGN KEY(user_id)
        REFERENCES end_user(id)
        ON DELETE CASCADE
)