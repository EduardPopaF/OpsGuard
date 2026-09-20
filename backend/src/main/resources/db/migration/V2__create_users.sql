CREATE TABLE users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    email VARCHAR(254) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_users_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT uk_users_organization_email
        UNIQUE (organization_id, email),

    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'MANAGER', 'ENGINEER', 'VIEWER')),

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE INDEX idx_users_organization_id
    ON users (organization_id);

CREATE INDEX idx_users_organization_status
    ON users (organization_id, status);

CREATE INDEX idx_users_organization_role
    ON users (organization_id, role);