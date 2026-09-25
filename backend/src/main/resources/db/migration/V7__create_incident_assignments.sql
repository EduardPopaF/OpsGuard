CREATE TABLE incident_assignments (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    assigned_team_id UUID,
    assigned_user_id UUID,
    assigned_by UUID,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    unassigned_at TIMESTAMP WITH TIME ZONE,
    reason VARCHAR(500),

    CONSTRAINT fk_incident_assignments_incident
        FOREIGN KEY (incident_id)
        REFERENCES incidents(id),

    CONSTRAINT fk_incident_assignments_team
        FOREIGN KEY (assigned_team_id)
        REFERENCES teams(id),

    CONSTRAINT fk_incident_assignments_user
        FOREIGN KEY (assigned_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_incident_assignments_assigned_by
        FOREIGN KEY (assigned_by)
        REFERENCES users(id),

    CONSTRAINT chk_incident_assignments_target
        CHECK (
            assigned_team_id IS NOT NULL
            OR assigned_user_id IS NOT NULL
        ),

    CONSTRAINT chk_incident_assignments_time_order
        CHECK (
            unassigned_at IS NULL
            OR unassigned_at >= assigned_at
        )
);

CREATE INDEX idx_incident_assignments_incident_id
    ON incident_assignments (incident_id);

CREATE INDEX idx_incident_assignments_team_id
    ON incident_assignments (assigned_team_id);

CREATE INDEX idx_incident_assignments_user_id
    ON incident_assignments (assigned_user_id);

CREATE INDEX idx_incident_assignments_assigned_by
    ON incident_assignments (assigned_by);

CREATE INDEX idx_incident_assignments_incident_assigned_at
    ON incident_assignments (incident_id, assigned_at);