CREATE TABLE teams (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_teams_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT chk_teams_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED'))
);

CREATE UNIQUE INDEX uk_teams_organization_name_ci
    ON teams (organization_id, LOWER(name));

CREATE INDEX idx_teams_organization_id
    ON teams (organization_id);

CREATE INDEX idx_teams_organization_status
    ON teams (organization_id, status);