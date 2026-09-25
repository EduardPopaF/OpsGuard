CREATE TABLE incident_timeline_events (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    actor_user_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    details TEXT,

    CONSTRAINT fk_incident_timeline_events_incident
        FOREIGN KEY (incident_id)
        REFERENCES incidents(id),

    CONSTRAINT fk_incident_timeline_events_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_incident_timeline_events_incident_id
    ON incident_timeline_events (incident_id);

CREATE INDEX idx_incident_timeline_events_actor_user_id
    ON incident_timeline_events (actor_user_id);

CREATE INDEX idx_incident_timeline_events_incident_occurred_at
    ON incident_timeline_events (incident_id, occurred_at);