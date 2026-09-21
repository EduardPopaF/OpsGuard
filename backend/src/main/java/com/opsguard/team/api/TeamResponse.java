package com.opsguard.team.api;

import com.opsguard.team.Team;
import com.opsguard.team.TeamStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        TeamStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getOrganization().getId(),
                team.getName(),
                team.getDescription(),
                team.getStatus(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }
}