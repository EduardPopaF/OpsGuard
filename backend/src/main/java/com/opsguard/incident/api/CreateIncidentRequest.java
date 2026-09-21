package com.opsguard.incident.api;

import com.opsguard.incident.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateIncidentRequest(

        @NotBlank(message = "Incident title is required.")
        @Size(
                max = 200,
                message = "Incident title must not exceed 200 characters."
        )
        String title,

        String description,

        @NotNull(message = "Incident severity is required.")
        IncidentSeverity severity,

        @NotNull(message = "Service ID is required.")
        UUID serviceId,

        @NotNull(message = "Creator user ID is required.")
        UUID createdByUserId
) {
}