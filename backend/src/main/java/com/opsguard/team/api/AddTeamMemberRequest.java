package com.opsguard.team.api;

import com.opsguard.team.TeamMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddTeamMemberRequest(

        @NotNull(message = "User ID is required.")
        UUID userId,

        @NotNull(message = "Membership role is required.")
        TeamMemberRole membershipRole
) {
}