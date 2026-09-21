CREATE TABLE services (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    owner_team_id UUID NOT NULL,
    criticality VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_services_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_services_owner_team
        FOREIGN KEY (owner_team_id)
        REFERENCES teams(id),

    CONSTRAINT chk_services_criticality
        CHECK (criticality IN (
            'CRITICAL',
            'HIGH',
            'MEDIUM',
            'LOW'
        )),

    CONSTRAINT chk_services_status
        CHECK (status IN (
            'ACTIVE',
            'DEACTIVATED'
        ))
);

CREATE UNIQUE INDEX uk_services_organization_name_ci
    ON services (organization_id, LOWER(name));

CREATE INDEX idx_services_organization_id
    ON services (organization_id);

CREATE INDEX idx_services_owner_team_id
    ON services (owner_team_id);

CREATE INDEX idx_services_organization_status
    ON services (organization_id, status);

CREATE INDEX idx_services_organization_criticality
    ON services (organization_id, criticality);