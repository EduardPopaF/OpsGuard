CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    membership_role VARCHAR(30) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id)
        REFERENCES teams(id),

    CONSTRAINT fk_team_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_team_members_team_user
        UNIQUE (team_id, user_id),

    CONSTRAINT chk_team_members_membership_role
        CHECK (membership_role IN ('LEAD', 'MEMBER'))
);

CREATE INDEX idx_team_members_team_id
    ON team_members (team_id);

CREATE INDEX idx_team_members_user_id
    ON team_members (user_id);