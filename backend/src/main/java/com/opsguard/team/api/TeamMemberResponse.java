package com.opsguard.team.api;

import com.opsguard.team.TeamMember;
import com.opsguard.team.TeamMemberRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        UUID teamId,
        UUID userId,
        TeamMemberRole membershipRole,
        OffsetDateTime joinedAt
) {

    public static TeamMemberResponse from(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getId(),
                teamMember.getTeam().getId(),
                teamMember.getUser().getId(),
                teamMember.getMembershipRole(),
                teamMember.getJoinedAt()
        );
    }
}