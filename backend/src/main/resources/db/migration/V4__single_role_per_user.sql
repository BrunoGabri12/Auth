ALTER TABLE end_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE end_user ADD CONSTRAINT chk_end_user_role CHECK (role IN ('ADMIN', 'MODERATOR', 'USER'));

-- Mantém a role mais alta de quem já tinha roles atribuídas
UPDATE end_user u
SET role = (
    SELECT r.role
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.user_id = u.id
    ORDER BY CASE r.role WHEN 'ADMIN' THEN 1 WHEN 'MODERATOR' THEN 2 ELSE 3 END
    LIMIT 1
)
WHERE EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id);

DROP TABLE user_roles;
DROP TABLE roles_permissions;
DROP TABLE roles;
DROP TABLE permissions;
