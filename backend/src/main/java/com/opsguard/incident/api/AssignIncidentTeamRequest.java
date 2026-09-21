package com.opsguard.incident.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignIncidentTeamRequest(

        @NotNull(message = "Team ID is required.")
        UUID teamId

) {
}