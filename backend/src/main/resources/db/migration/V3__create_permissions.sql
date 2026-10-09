CREATE TABLE permissions (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE roles (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT chk_roles_role CHECK (role IN ('ADMIN', 'MODERATOR', 'USER'))
);

CREATE TABLE roles_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,

    CONSTRAINT pk_roles_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_roles_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_roles_permissions_permission_id ON roles_permissions(permission_id);

CREATE TABLE user_roles (
    user_id UUID   NOT NULL,
    role_id BIGINT NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES end_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

INSERT INTO roles (role) VALUES ('ADMIN'), ('MODERATOR'), ('USER');