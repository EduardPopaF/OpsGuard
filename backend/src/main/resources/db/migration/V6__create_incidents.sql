CREATE TABLE incident_counters (
    organization_id UUID PRIMARY KEY,
    next_number BIGINT NOT NULL,

    CONSTRAINT fk_incident_counters_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT chk_incident_counters_next_number
        CHECK (next_number > 0)
);

CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    incident_number VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    severity VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    service_id UUID NOT NULL,
    assigned_team_id UUID,
    assigned_user_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_incidents_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_incidents_service
        FOREIGN KEY (service_id)
        REFERENCES services(id),

    CONSTRAINT fk_incidents_assigned_team
        FOREIGN KEY (assigned_team_id)
        REFERENCES teams(id),

    CONSTRAINT fk_incidents_assigned_user
        FOREIGN KEY (assigned_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_incidents_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT chk_incidents_severity
        CHECK (severity IN (
            'SEV1',
            'SEV2',
            'SEV3',
            'SEV4'
        )),

    CONSTRAINT chk_incidents_status
        CHECK (status IN (
            'OPEN',
            'ACKNOWLEDGED',
            'INVESTIGATING',
            'MITIGATED',
            'MONITORING',
            'RESOLVED',
            'CLOSED'
        )),

    CONSTRAINT uk_incidents_organization_number
        UNIQUE (organization_id, incident_number)
);

CREATE INDEX idx_incidents_organization_id
    ON incidents (organization_id);

CREATE INDEX idx_incidents_organization_status
    ON incidents (organization_id, status);

CREATE INDEX idx_incidents_organization_severity
    ON incidents (organization_id, severity);

CREATE INDEX idx_incidents_service_id
    ON incidents (service_id);

CREATE INDEX idx_incidents_assigned_team_id
    ON incidents (assigned_team_id);

CREATE INDEX idx_incidents_assigned_user_id
    ON incidents (assigned_user_id);

CREATE INDEX idx_incidents_created_at
    ON incidents (created_at);