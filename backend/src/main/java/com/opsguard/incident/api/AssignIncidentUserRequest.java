package com.opsguard.incident.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignIncidentUserRequest(

        @NotNull(message = "User ID is required.")
        UUID userId

) {
}